package com.fuzzycontroller.service;

import com.fuzzycontroller.model.ControlRequest;
import com.fuzzycontroller.model.ControlResponse;
import com.fuzzycontroller.model.Environment;
import com.fuzzycontroller.model.SimulationSnapshot;
import com.fuzzycontroller.model.VehicleState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService {

    private static final double DT = 0.1;
    private static final double MAX_ACCEL = 7.0;
    private static final double MAX_LATERAL_RATE = 1.2;
    private static final double MS_TO_KMH = 3.6;
    private static final double LATERAL_LIMIT = 2.0;

    private static final double LANE_CHANGE_TRIGGER = 0.6;
    private static final double LANE_CHANGE_RATE = 0.05;
    private static final double CLEAR_ROAD_DISTANCE = 200.0;
    private static final double CURVE_OFFSET_GAIN = 20.0;

    private final ScenarioService scenarioService;
    private final FuzzyEngineService fuzzyEngine;

    public SimulationService(ScenarioService scenarioService, FuzzyEngineService fuzzyEngine) {
        this.scenarioService = scenarioService;
        this.fuzzyEngine = fuzzyEngine;
    }

    public List<SimulationSnapshot> run(String scenarioId, Double durationOverride) {
        Scenario scenario = scenarioService.get(scenarioId);
        double duration = durationOverride != null && durationOverride > 0
            ? durationOverride
            : scenario.defaultDuration();
        int steps = (int) Math.round(duration / DT);

        VehicleState own = scenario.initialVehicle();
        List<SimulationSnapshot> snapshots = new ArrayList<>(steps + 1);

        for (int i = 0; i <= steps; i++) {
            double t = i * DT;
            Environment env = scenario.environmentAt(t, own);

            double rawDistance = env.leadPosition() - own.position();
            double rawRelativeSpeed = env.leadSpeed() - own.speed();

            double effectiveDistance = own.lane() > 0.5
                ? CLEAR_ROAD_DISTANCE
                : rawDistance;
            double effectiveRelativeSpeed = own.lane() > 0.5
                ? 0.0
                : rawRelativeSpeed;

            ControlRequest req = new ControlRequest(
                effectiveDistance,
                effectiveRelativeSpeed,
                own.speed() * MS_TO_KMH,
                env.roadCondition(),
                env.curvature(),
                own.lateralOffset()
            );
            ControlResponse control = fuzzyEngine.evaluate(req);

            snapshots.add(new SimulationSnapshot(
                t,
                own.position(),
                own.speed(),
                env.leadPosition(),
                env.leadSpeed(),
                own.lateralOffset(),
                own.lane(),
                env.curvature(),
                env.roadCondition(),
                rawDistance,
                rawRelativeSpeed,
                control.acceleration(),
                control.steeringCorrection(),
                control.laneChangeUrgency(),
                control.laneChangeAdvice(),
                env.leftLaneBlocked(),
                control.activatedRules()
            ));

            own = integrate(own, env, control, t);
        }

        return snapshots;
    }

    private VehicleState integrate(VehicleState own, Environment env, ControlResponse control, double t) {
        double newSpeed = own.speed() + control.acceleration() * MAX_ACCEL * DT;
        if (newSpeed < 0.0) newSpeed = 0.0;

        double newPosition = own.position() + newSpeed * DT;

        double netSteering = control.steeringCorrection() - env.curvature() * CURVE_OFFSET_GAIN;
        double newOffset = own.lateralOffset() + netSteering * MAX_LATERAL_RATE * DT;
        if (newOffset >  LATERAL_LIMIT) newOffset =  LATERAL_LIMIT;
        if (newOffset < -LATERAL_LIMIT) newOffset = -LATERAL_LIMIT;

        double newLane = own.lane();
        boolean midChange    = own.lane() > 0.01 && own.lane() < 0.99;
        boolean shouldChange = own.lane() < 0.99
            && control.laneChangeUrgency() > LANE_CHANGE_TRIGGER
            && !env.leftLaneBlocked();

        if (shouldChange || midChange) {
            newLane = Math.min(1.0, own.lane() + LANE_CHANGE_RATE);
        }

        return new VehicleState(t + DT, newPosition, newSpeed, newOffset, newLane);
    }
}
