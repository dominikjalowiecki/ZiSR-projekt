package com.fuzzycontroller.service;

import com.fuzzycontroller.model.ControlRequest;
import com.fuzzycontroller.model.ControlResponse;
import com.fuzzycontroller.model.RuleActivation;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import fuzzlib.FuzzySet;
import fuzzlib.DefuzMethod;
import fuzzlib.norms.SNorm;
import fuzzlib.norms.TNorm;
import fuzzlib.reasoning.ReasoningSystem;
import fuzzlib.reasoning.SystemConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FuzzyEngineService {

    private static final int IN_DISTANCE   = 0;
    private static final int IN_REL_SPEED  = 1;
    private static final int IN_OWN_SPEED  = 2;
    private static final int IN_ROAD       = 3;
    private static final int IN_CURVE      = 4;
    private static final int IN_OFFSET     = 5;
    private static final int IN_COUNT      = 6;

    private static final double DISTANCE_MIN = 0.0,    DISTANCE_MAX = 200.0;
    private static final double REL_SPEED_MIN = -30.0, REL_SPEED_MAX = 30.0;
    private static final double OWN_SPEED_MIN = 0.0,   OWN_SPEED_MAX = 180.0;
    private static final double ROAD_MIN = 0.0,        ROAD_MAX = 1.0;
    private static final double CURVE_MIN = -0.05,     CURVE_MAX = 0.05;
    private static final double OFFSET_MIN = -2.0,     OFFSET_MAX = 2.0;

    private static final double ACCEL_MIN = -1.0, ACCEL_MAX = 1.0;
    private static final double STEER_MIN = -1.0, STEER_MAX = 1.0;
    private static final double LANE_MIN  = 0.0,  LANE_MAX  = 1.0;

    private ReasoningSystem rsAccel;
    private ReasoningSystem rsSteer;
    private ReasoningSystem rsLane;

    private final Map<String, Map<String, FuzzySet>> premiseSetsByVariable = new LinkedHashMap<>();

    private record AntecedentSpec(String variable, String set) {}
    private record RuleDef(String name, String outputVar, List<AntecedentSpec> antecedents) {}
    private final List<RuleDef> ruleDefs = new ArrayList<>();

    @PostConstruct
    void init() {
        defineDistanceSets();
        defineRelSpeedSets();
        defineOwnSpeedSets();
        defineRoadSets();
        defineCurveSets();
        defineOffsetSets();

        rsAccel = buildSystem();
        configureInputVars(rsAccel);
        rsAccel.describeOutputVar(0, "acceleration", "");
        registerAllPremiseSets(rsAccel);
        addAccelConclusionSets(rsAccel);

        rsSteer = buildSystem();
        configureInputVars(rsSteer);
        rsSteer.describeOutputVar(0, "steeringCorrection", "");
        registerAllPremiseSets(rsSteer);
        addSteerConclusionSets(rsSteer);

        rsLane = buildSystem();
        configureInputVars(rsLane);
        rsLane.describeOutputVar(0, "laneChangeUrgency", "");
        registerAllPremiseSets(rsLane);
        addLaneConclusionSets(rsLane);

        try {
            addAccelRules();
            addSteerRules();
            addLaneRules();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure fuzzy rules", e);
        }
    }

    public synchronized ControlResponse evaluate(ControlRequest request) {
        double distance = clamp(request.distance(),       DISTANCE_MIN,  DISTANCE_MAX);
        double relSpeed = clamp(request.relativeSpeed(),  REL_SPEED_MIN, REL_SPEED_MAX);
        double ownSpeed = clamp(request.ownSpeed(),       OWN_SPEED_MIN, OWN_SPEED_MAX);
        double road     = clamp(request.roadCondition(),  ROAD_MIN,      ROAD_MAX);
        double curve    = clamp(request.curvature(),      CURVE_MIN,     CURVE_MAX);
        double offset   = clamp(request.lateralOffset(),  OFFSET_MIN,    OFFSET_MAX);

        double[] inputs = { distance, relSpeed, ownSpeed, road, curve, offset };

        applyInputs(rsAccel, inputs);
        rsAccel.Process();
        double acceleration = safeDefuzzify(rsAccel.getOutputVar(0).outset, ACCEL_MIN, ACCEL_MAX);

        applyInputs(rsSteer, inputs);
        rsSteer.Process();
        double steering = safeDefuzzify(rsSteer.getOutputVar(0).outset, STEER_MIN, STEER_MAX);

        applyInputs(rsLane, inputs);
        rsLane.Process();
        double laneUrgency = safeDefuzzify(rsLane.getOutputVar(0).outset, LANE_MIN, LANE_MAX);

        Map<String, Double> inputValues = Map.of(
                "distance",       distance,
                "relativeSpeed",  relSpeed,
                "ownSpeed",       ownSpeed,
                "roadCondition",  road,
                "curvature",      curve,
                "lateralOffset",  offset
        );
        Map<String, Map<String, Double>> fuzzified = fuzzifyAll(inputValues);
        List<RuleActivation> activations = collectActivations(fuzzified);
        String advice = adviceLabel(laneUrgency);

        return new ControlResponse(acceleration, steering, laneUrgency, advice, activations, fuzzified);
    }

    public Map<String, Object> describe() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("inputs", List.of(
                inputDescriptor("distance",      "m",     DISTANCE_MIN,  DISTANCE_MAX),
                inputDescriptor("relativeSpeed", "m/s",   REL_SPEED_MIN, REL_SPEED_MAX),
                inputDescriptor("ownSpeed",      "km/h",  OWN_SPEED_MIN, OWN_SPEED_MAX),
                inputDescriptor("roadCondition", "0..1",  ROAD_MIN,      ROAD_MAX),
                inputDescriptor("curvature",     "1/m",   CURVE_MIN,     CURVE_MAX),
                inputDescriptor("lateralOffset", "m",     OFFSET_MIN,    OFFSET_MAX)
        ));

        result.put("outputs", List.of(
                Map.of("name", "acceleration",       "min", ACCEL_MIN, "max", ACCEL_MAX,
                        "sets", List.of("brakeHard", "brakeSoft", "coast", "accelSoft", "accelHard")),
                Map.of("name", "steeringCorrection", "min", STEER_MIN, "max", STEER_MAX,
                        "sets", List.of("leftStrong", "leftMild", "neutral", "rightMild", "rightStrong")),
                Map.of("name", "laneChangeUrgency",  "min", LANE_MIN,  "max", LANE_MAX,
                        "sets", List.of("keep", "prepare", "changeNow"))
        ));

        result.put("rules", ruleDefs.stream().map(r -> r.name).toList());

        return result;
    }

    private ReasoningSystem buildSystem() {
        SystemConfig cfg = new SystemConfig();

        cfg.setInputWidth(IN_COUNT);
        cfg.setOutputWidth(1);
        cfg.setNumberOfPremiseSets(64);
        cfg.setNumberOfConclusionSets(8);
        cfg.setIsOperationType(TNorm.TN_PRODUCT);
        cfg.setAndOperationType(TNorm.TN_MINIMUM);
        cfg.setOrOperationType(SNorm.SN_MAXIMUM);
        cfg.setImplicationType(TNorm.TN_MINIMUM);
        cfg.setConclusionAgregationType(SNorm.SN_MAXIMUM);
        cfg.setAutoDefuzzyfication(false);
        cfg.setDefuzzyfication(DefuzMethod.DF_COG);

        return new ReasoningSystem(cfg);
    }

    private void configureInputVars(ReasoningSystem rs) {
        rs.describeInputVar(IN_DISTANCE,  "distance",      "");
        rs.describeInputVar(IN_REL_SPEED, "relativeSpeed", "");
        rs.describeInputVar(IN_OWN_SPEED, "ownSpeed",      "");
        rs.describeInputVar(IN_ROAD,      "roadCondition", "");
        rs.describeInputVar(IN_CURVE,     "curvature",     "");
        rs.describeInputVar(IN_OFFSET,    "lateralOffset", "");
    }

    private void registerAllPremiseSets(ReasoningSystem rs) {
        for (Map<String, FuzzySet> sets : premiseSetsByVariable.values()) {
            for (FuzzySet fs : sets.values()) {
                rs.addPremiseSet(fs);
            }
        }
    }

    private void applyInputs(ReasoningSystem rs, double[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            rs.setInput(i, inputs[i]);
        }
    }

    private void defineDistanceSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("distVeryClose", gaussian("distVeryClose",   0.0,  0.0,  25.0));
        sets.put("distClose",     gaussian("distClose",       30.0, 25.0, 35.0));
        sets.put("distSafe",      gaussian("distSafe",        90.0, 35.0, 45.0));
        sets.put("distFar",       gaussian("distFar",         180.0, 65.0, 0.0));

        premiseSetsByVariable.put("distance", sets);
    }

    private void defineRelSpeedSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("relApproachingFast", gaussian("relApproachingFast", -15.0, 0.0, 10.0));
        sets.put("relApproaching",     gaussian("relApproaching",      -5.0, 5.0, 4.0));
        sets.put("relStable",          gaussian("relStable",            0.0, 3.0, 3.0));
        sets.put("relReceding",        gaussian("relReceding",          8.0, 4.0, 0.0));

        premiseSetsByVariable.put("relativeSpeed", sets);
    }

    private void defineOwnSpeedSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("speedLow",      gaussian("speedLow",       30.0,  30.0, 30.0));
        sets.put("speedMedium",   gaussian("speedMedium",    70.0,  30.0, 30.0));
        sets.put("speedHigh",     gaussian("speedHigh",     110.0,  30.0, 30.0));
        sets.put("speedVeryHigh", gaussian("speedVeryHigh", 160.0,  30.0, 0.0));

        premiseSetsByVariable.put("ownSpeed", sets);
    }

    private void defineRoadSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("roadPoor",     gaussian("roadPoor",     0.0,  0.0,  0.4));
        sets.put("roadModerate", gaussian("roadModerate", 0.55, 0.3,  0.3));
        sets.put("roadGood",     gaussian("roadGood",     1.0,  0.4,  0.0));

        premiseSetsByVariable.put("roadCondition", sets);
    }

    private void defineCurveSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("curveSharpLeft",   gaussian("curveSharpLeft",   -0.04, 0.0,   0.025));
        sets.put("curveGentleLeft",  gaussian("curveGentleLeft",  -0.015, 0.025, 0.015));
        sets.put("curveStraight",    gaussian("curveStraight",     0.0,  0.015, 0.015));
        sets.put("curveGentleRight", gaussian("curveGentleRight",  0.015, 0.015, 0.025));
        sets.put("curveSharpRight",  gaussian("curveSharpRight",   0.04, 0.025, 0.0));

        premiseSetsByVariable.put("curvature", sets);
    }

    private void defineOffsetSets() {
        Map<String, FuzzySet> sets = new LinkedHashMap<>();

        sets.put("offsetLeftFar",   gaussian("offsetLeftFar",   -1.5, 0.5, 0.7));
        sets.put("offsetLeftNear",  gaussian("offsetLeftNear",  -0.6, 0.7, 0.5));
        sets.put("offsetCenter",    gaussian("offsetCenter",     0.0, 0.4, 0.4));
        sets.put("offsetRightNear", gaussian("offsetRightNear",  0.6, 0.5, 0.7));
        sets.put("offsetRightFar",  gaussian("offsetRightFar",   1.5, 0.7, 0.5));
        
        premiseSetsByVariable.put("lateralOffset", sets);
    }

    private void addAccelConclusionSets(ReasoningSystem rs) {
        rs.addConclusionSet(conclusionGaussian("accBrakeHard", -0.85, 0.0,  0.45));
        rs.addConclusionSet(conclusionGaussian("accBrakeSoft", -0.4,  0.45, 0.4));
        rs.addConclusionSet(conclusionGaussian("accCoast",      0.0,  0.4,  0.4));
        rs.addConclusionSet(conclusionGaussian("accAccelSoft",  0.4,  0.4,  0.45));
        rs.addConclusionSet(conclusionGaussian("accAccelHard",  0.85, 0.45, 0.0));
    }

    private void addSteerConclusionSets(ReasoningSystem rs) {
        rs.addConclusionSet(conclusionGaussian("steerLeftStrong",  -0.8, 0.0,  0.5));
        rs.addConclusionSet(conclusionGaussian("steerLeftMild",    -0.3, 0.5,  0.3));
        rs.addConclusionSet(conclusionGaussian("steerNeutral",      0.0, 0.3,  0.3));
        rs.addConclusionSet(conclusionGaussian("steerRightMild",    0.3, 0.3,  0.5));
        rs.addConclusionSet(conclusionGaussian("steerRightStrong",  0.8, 0.5,  0.0));
    }

    private void addLaneConclusionSets(ReasoningSystem rs) {
        rs.addConclusionSet(conclusionGaussian("laneKeep",      0.0, 0.0, 0.35));
        rs.addConclusionSet(conclusionGaussian("lanePrepare",   0.5, 0.3, 0.3));
        rs.addConclusionSet(conclusionGaussian("laneChangeNow", 1.0, 0.35, 0.0));
    }

    private void addAccelRules() throws Exception {
        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI odległość bardzo blisko TO mocne hamowanie",
                ant("distance", "distVeryClose"));

        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI odległość blisko I prędk. wzgl. szybko zbliża TO mocne hamowanie",
                ant("distance", "distClose"), and("relativeSpeed", "relApproachingFast"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI odległość blisko I prędk. wzgl. zbliża się TO łagodne hamowanie",
                ant("distance", "distClose"), and("relativeSpeed", "relApproaching"));

        addRule(rsAccel, "acceleration", "accCoast",
                "JEŻELI odległość blisko I prędk. wzgl. stabilna TO toczenie",
                ant("distance", "distClose"), and("relativeSpeed", "relStable"));

        addRule(rsAccel, "acceleration", "accCoast",
                "JEŻELI odległość bezpieczna I prędk. wzgl. stabilna TO toczenie",
                ant("distance", "distSafe"), and("relativeSpeed", "relStable"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI odległość bezpieczna I prędk. wzgl. zbliża się TO łagodne hamowanie",
                ant("distance", "distSafe"), and("relativeSpeed", "relApproaching"));

        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI odległość bezpieczna I prędk. wzgl. szybko zbliża TO mocne hamowanie",
                ant("distance", "distSafe"), and("relativeSpeed", "relApproachingFast"));

        addRule(rsAccel, "acceleration", "accAccelSoft",
                "JEŻELI odległość bezpieczna I prędk. wzgl. oddala się TO łagodne przyspieszenie",
                ant("distance", "distSafe"), and("relativeSpeed", "relReceding"));

        addRule(rsAccel, "acceleration", "accAccelHard",
                "JEŻELI odległość daleko I prędk. własna niska TO mocne przyspieszenie",
                ant("distance", "distFar"), and("ownSpeed", "speedLow"));

        addRule(rsAccel, "acceleration", "accAccelSoft",
                "JEŻELI odległość daleko I prędk. własna średnia TO łagodne przyspieszenie",
                ant("distance", "distFar"), and("ownSpeed", "speedMedium"));

        addRule(rsAccel, "acceleration", "accCoast",
                "JEŻELI odległość daleko I prędk. własna wysoka TO toczenie",
                ant("distance", "distFar"), and("ownSpeed", "speedHigh"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI odległość daleko I prędk. własna bardzo wysoka TO łagodne hamowanie",
                ant("distance", "distFar"), and("ownSpeed", "speedVeryHigh"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI droga zła I prędk. własna wysoka TO łagodne hamowanie",
                ant("roadCondition", "roadPoor"), and("ownSpeed", "speedHigh"));

        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI droga zła I prędk. własna bardzo wysoka TO mocne hamowanie",
                ant("roadCondition", "roadPoor"), and("ownSpeed", "speedVeryHigh"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI zakręt ostry w lewo I prędk. własna wysoka TO łagodne hamowanie",
                ant("curvature", "curveSharpLeft"), and("ownSpeed", "speedHigh"));

        addRule(rsAccel, "acceleration", "accBrakeSoft",
                "JEŻELI zakręt ostry w prawo I prędk. własna wysoka TO łagodne hamowanie",
                ant("curvature", "curveSharpRight"), and("ownSpeed", "speedHigh"));

        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI zakręt ostry w lewo I prędk. własna bardzo wysoka TO mocne hamowanie",
                ant("curvature", "curveSharpLeft"), and("ownSpeed", "speedVeryHigh"));

        addRule(rsAccel, "acceleration", "accBrakeHard",
                "JEŻELI zakręt ostry w prawo I prędk. własna bardzo wysoka TO mocne hamowanie",
                ant("curvature", "curveSharpRight"), and("ownSpeed", "speedVeryHigh"));
    }

    private void addSteerRules() throws Exception {
        addRule(rsSteer, "steeringCorrection", "steerRightStrong",
                "JEŻELI odchylenie daleko w lewo TO mocno w prawo",
                ant("lateralOffset", "offsetLeftFar"));
        addRule(rsSteer, "steeringCorrection", "steerRightMild",
                "JEŻELI odchylenie blisko w lewo TO łagodnie w prawo",
                ant("lateralOffset", "offsetLeftNear"));
        addRule(rsSteer, "steeringCorrection", "steerNeutral",
                "JEŻELI odchylenie środek TO neutralnie",
                ant("lateralOffset", "offsetCenter"));
        addRule(rsSteer, "steeringCorrection", "steerLeftMild",
                "JEŻELI odchylenie blisko w prawo TO łagodnie w lewo",
                ant("lateralOffset", "offsetRightNear"));
        addRule(rsSteer, "steeringCorrection", "steerLeftStrong",
                "JEŻELI odchylenie daleko w prawo TO mocno w lewo",
                ant("lateralOffset", "offsetRightFar"));
        addRule(rsSteer, "steeringCorrection", "steerLeftMild",
                "JEŻELI zakręt łagodny w lewo TO łagodnie w lewo",
                ant("curvature", "curveGentleLeft"));
        addRule(rsSteer, "steeringCorrection", "steerRightMild",
                "JEŻELI zakręt łagodny w prawo TO łagodnie w prawo",
                ant("curvature", "curveGentleRight"));
        addRule(rsSteer, "steeringCorrection", "steerLeftStrong",
                "JEŻELI zakręt ostry w lewo TO mocno w lewo",
                ant("curvature", "curveSharpLeft"));
        addRule(rsSteer, "steeringCorrection", "steerRightStrong",
                "JEŻELI zakręt ostry w prawo TO mocno w prawo",
                ant("curvature", "curveSharpRight"));
    }

    private void addLaneRules() throws Exception {
        addRule(rsLane, "laneChangeUrgency", "laneChangeNow",
                "JEŻELI odległość blisko I prędk. wzgl. szybko zbliża TO zmień pas",
                ant("distance", "distClose"), and("relativeSpeed", "relApproachingFast"));
        addRule(rsLane, "laneChangeUrgency", "lanePrepare",
                "JEŻELI odległość blisko I prędk. wzgl. zbliża się TO przygotuj zmianę",
                ant("distance", "distClose"), and("relativeSpeed", "relApproaching"));
        addRule(rsLane, "laneChangeUrgency", "laneChangeNow",
                "JEŻELI odległość bezpieczna I prędk. wzgl. szybko zbliża TO zmień pas",
                ant("distance", "distSafe"), and("relativeSpeed", "relApproachingFast"));
        addRule(rsLane, "laneChangeUrgency", "laneChangeNow",
                "JEŻELI odległość bardzo blisko TO zmień pas",
                ant("distance", "distVeryClose"));
        addRule(rsLane, "laneChangeUrgency", "lanePrepare",
                "JEŻELI odległość blisko TO przygotuj zmianę",
                ant("distance", "distClose"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI odległość bezpieczna TO utrzymaj pas",
                ant("distance", "distSafe"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI odległość daleko TO utrzymaj pas",
                ant("distance", "distFar"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI droga zła TO utrzymaj pas",
                ant("roadCondition", "roadPoor"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI zakręt ostry w lewo TO utrzymaj pas",
                ant("curvature", "curveSharpLeft"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI zakręt ostry w prawo TO utrzymaj pas",
                ant("curvature", "curveSharpRight"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI odległość blisko I prędk. własna niska TO utrzymaj pas",
                ant("distance", "distClose"), and("ownSpeed", "speedLow"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI odległość bardzo blisko I prędk. własna niska TO utrzymaj pas",
                ant("distance", "distVeryClose"), and("ownSpeed", "speedLow"));
        addRule(rsLane, "laneChangeUrgency", "laneKeep",
                "JEŻELI prędk. wzgl. oddala się TO utrzymaj pas",
                ant("relativeSpeed", "relReceding"));
    }

    private record Antecedent(String var, String set, String op) {}

    private static Antecedent ant(String var, String set) {
        return new Antecedent(var, set, null);
    }

    private static Antecedent and(String var, String set) {
        return new Antecedent(var, set, "AND");
    }

    private void addRule(ReasoningSystem rs, String outVar, String outSet,
                         String name, Antecedent... antecedents) throws Exception {
        rs.addRule(antecedents.length, 1);
        if (antecedents.length == 1) {
            rs.addRuleItem(antecedents[0].var, antecedents[0].set);
        } else if (antecedents.length == 2) {
            rs.addRuleItem(antecedents[0].var, antecedents[0].set,
                    antecedents[1].op, antecedents[1].var, antecedents[1].set);
        } else {
            throw new IllegalArgumentException("Max 2 antecedents supported");
        }

        rs.addRuleConclusion(outVar, outSet);

        List<AntecedentSpec> specs = new ArrayList<>();

        for (Antecedent a : antecedents) {
            specs.add(new AntecedentSpec(a.var, a.set));
        }

        ruleDefs.add(new RuleDef(name, outVar, specs));
    }

    private Map<String, Map<String, Double>> fuzzifyAll(Map<String, Double> inputValues) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, FuzzySet>> e : premiseSetsByVariable.entrySet()) {
            String varName = e.getKey();
            double value = inputValues.getOrDefault(varName, 0.0);
            Map<String, Double> memberships = new LinkedHashMap<>();
            
            for (Map.Entry<String, FuzzySet> setEntry : e.getValue().entrySet()) {
                memberships.put(setEntry.getKey(), setEntry.getValue().getMembership(value));
            }

            result.put(varName, memberships);
        }
        return result;
    }

    private List<RuleActivation> collectActivations(Map<String, Map<String, Double>> fuzzified) {
        List<RuleActivation> activations = new ArrayList<>();

        for (RuleDef def : ruleDefs) {
            double activation = 1.0;
            for (AntecedentSpec a : def.antecedents) {
                Map<String, Double> mem = fuzzified.get(a.variable);
                double m = (mem == null) ? 0.0 : mem.getOrDefault(a.set, 0.0);
                activation = Math.min(activation, m);
            }
            
            if (activation > RULE_ACTIVATION_DISPLAY_THRESHOLD) {
                activations.add(new RuleActivation(def.name, def.outputVar, activation));
            }
        }
        
        activations.sort((x, y) -> Double.compare(y.activation(), x.activation()));

        return activations;
    }

    private String adviceLabel(double urgency) {
        if (urgency < 0.33) return "KEEP";
        if (urgency < 0.66) return "PREPARE";
        return "CHANGE_NOW";
    }

    private static final double GAUSS_SIGMA_FACTOR = 2.5;
    private static final double GAUSS_SHOULDER_FACTOR = 4.0;
    private static final double RULE_ACTIVATION_DISPLAY_THRESHOLD = 0.05;

    private FuzzySet gaussian(String id, double center, double leftHalfWidth, double rightHalfWidth) {
        FuzzySet set = new FuzzySet(id, "");

        boolean leftShoulder  = leftHalfWidth  <= 0.0;
        boolean rightShoulder = rightHalfWidth <= 0.0;

        double leftSigma  = leftShoulder  ? 0.0 : leftHalfWidth  / GAUSS_SIGMA_FACTOR;
        double rightSigma = rightShoulder ? 0.0 : rightHalfWidth / GAUSS_SIGMA_FACTOR;

        if (leftShoulder && rightShoulder) {
            leftSigma = rightSigma = 0.01;
        } else if (leftShoulder) {
            leftSigma = rightSigma * GAUSS_SHOULDER_FACTOR;
        } else if (rightShoulder) {
            rightSigma = leftSigma * GAUSS_SHOULDER_FACTOR;
        }

        set.newGaussian(center, leftSigma, rightSigma);

        return set;
    }

    private FuzzySet conclusionGaussian(String id, double center, double leftHalfWidth, double rightHalfWidth) {
        FuzzySet set = new FuzzySet(id, "");

        boolean leftShoulder  = leftHalfWidth  <= 0.0;
        boolean rightShoulder = rightHalfWidth <= 0.0;

        double leftSigma  = leftShoulder  ? 0.0 : leftHalfWidth  / GAUSS_SIGMA_FACTOR;
        double rightSigma = rightShoulder ? 0.0 : rightHalfWidth / GAUSS_SIGMA_FACTOR;

        if (leftShoulder && rightShoulder) {
            leftSigma = rightSigma = 0.01;
        } else if (leftShoulder) {
            leftSigma = rightSigma;
        } else if (rightShoulder) {
            rightSigma = leftSigma;
        }

        set.newGaussian(center, leftSigma, rightSigma);

        return set;
    }

    private double safeDefuzzify(FuzzySet outset, double lo, double hi) {
        if (outset == null) return 0.0;

        double v = outset.DeFuzzyfy();

        if (Double.isNaN(v) || Double.isInfinite(v)) return 0.0;

        return clamp(v, lo, hi);
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private Map<String, Object> inputDescriptor(String name, String unit, double min, double max) {
        Map<String, Object> d = new LinkedHashMap<>();

        d.put("name", name);
        d.put("unit", unit);
        d.put("min", min);
        d.put("max", max);

        Map<String, FuzzySet> sets = premiseSetsByVariable.get(name);

        d.put("sets", sets == null ? List.of() : new ArrayList<>(sets.keySet()));

        return d;
    }
}
