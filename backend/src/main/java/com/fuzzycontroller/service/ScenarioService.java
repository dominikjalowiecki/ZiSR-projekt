package com.fuzzycontroller.service;

import com.fuzzycontroller.model.Environment;
import com.fuzzycontroller.model.ScenarioDescriptor;
import com.fuzzycontroller.model.VehicleState;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioService {

    private static final double KMH_TO_MS = 1.0 / 3.6;

    private final Map<String, Scenario> scenarios = new LinkedHashMap<>();

    public ScenarioService() {
        scenarios.put("highway-cruise", buildHighwayCruise());
        scenarios.put("lead-slowdown",  buildLeadSlowdown());
        scenarios.put("rain-curve",     buildRainCurve());
        scenarios.put("cut-in",         buildCutIn());
        scenarios.put("stop-and-go",    buildStopAndGo());
        scenarios.put("mountain-road",  buildMountainRoad());
        scenarios.put("lane-drift",     buildLaneDrift());
    }

    public List<ScenarioDescriptor> list() {
        return scenarios.values().stream()
            .map(s -> new ScenarioDescriptor(s.id(), s.label(), s.description(), s.defaultDuration()))
            .toList();
    }

    public Scenario get(String id) {
        Scenario s = scenarios.get(id);
        if (s == null) throw new IllegalArgumentException("Unknown scenario: " + id);

        return s;
    }

    private Scenario buildHighwayCruise() {
        double ownSpeed  = 90 * KMH_TO_MS;
        double leadSpeed = 90 * KMH_TO_MS;
        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(80.0, leadSpeed, 0.0, 0.9);

        return new Scenario(
            "highway-cruise",
            "Autostrada",
            "Stała prędkość, prosta droga, sucho. Sterownik ma utrzymać status quo.",
            10.0,
            initial,
            env0,
            (t, own) -> new Environment(
                env0.leadPosition() + leadSpeed * t,
                leadSpeed,
                0.0,
                0.9
            )
        );
    }

    private Scenario buildLeadSlowdown() {
        double ownSpeed   = 90 * KMH_TO_MS;
        double leadSpeed0 = 90 * KMH_TO_MS;
        double brakeStart = 2.0;
        double leadDecel  = 3.0;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(80.0, leadSpeed0, 0.0, 0.9, true);

        return new Scenario(
            "lead-slowdown",
            "Auto z przodu hamuje",
            "Po 2 s pojazd poprzedzający zaczyna hamować. Lewy pas zajęty.",
            12.0,
            initial,
            env0,
            (t, own) -> {
                double leadSpeed = t < brakeStart
                    ? leadSpeed0
                    : Math.max(0.0, leadSpeed0 - leadDecel * (t - brakeStart));
                double leadPos = leadPositionAt(env0.leadPosition(), leadSpeed0, leadDecel, brakeStart, t);

                return new Environment(leadPos, leadSpeed, 0.0, 0.9, true);
            }
        );
    }

    private Scenario buildRainCurve() {
        double ownSpeed  = 110 * KMH_TO_MS;
        double leadSpeed = 110 * KMH_TO_MS;
        double rainStart  = 2.0;
        double curveStart = 3.0;
        double curveValue = -0.03;
        double roadWet    = 0.2;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(120.0, leadSpeed, 0.0, 0.9);

        return new Scenario(
            "rain-curve",
            "Deszcz i zakręt w lewo",
            "Po 2 s zaczyna padać, po 3 s droga skręca w lewo.",
            12.0,
            initial,
            env0,
            (t, own) -> {
                double road = t < rainStart  ? 0.9        : roadWet;
                double curve = t < curveStart ? 0.0       : curveValue;

                return new Environment(
                    env0.leadPosition() + leadSpeed * t,
                    leadSpeed,
                    curve,
                    road
                );
            }
        );
    }

    private double leadPositionAt(double p0, double v0, double decel, double brakeStart, double t) {
        if (t <= brakeStart) {
            return p0 + v0 * t;
        }

        double posAtBrake = p0 + v0 * brakeStart;
        double dt = t - brakeStart;
        double timeToStop = v0 / decel;

        if (dt >= timeToStop) {
            return posAtBrake + 0.5 * v0 * timeToStop;
        }

        return posAtBrake + v0 * dt - 0.5 * decel * dt * dt;
    }

    private Scenario buildCutIn() {
        double ownSpeed = 130 * KMH_TO_MS;
        double cruiseSpeed = ownSpeed;
        double cutInTime = 3.0;
        double cutInGap = 25.0;
        double leadSpeedAfter = 90 * KMH_TO_MS;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(250.0, cruiseSpeed, 0.0, 0.95, true);

        return new Scenario(
            "cut-in",
            "Wjazd z sąsiedniego pasa",
            "Pusta autostrada. Po 3 s tuż przed nami wjeżdża wolniejsze auto. Lewy pas zajęty.",
            10.0,
            initial,
            env0,
            (t, own) -> {
                if (t < cutInTime) {
                    return new Environment(env0.leadPosition() + cruiseSpeed * t, cruiseSpeed, 0.0, 0.95, true);
                }

                double anchorPos = cruiseSpeed * cutInTime + cutInGap;
                double leadPos = anchorPos + leadSpeedAfter * (t - cutInTime);

                return new Environment(leadPos, leadSpeedAfter, 0.0, 0.95, true);
            }
        );
    }

    private Scenario buildStopAndGo() {
        double v0 = 60 * KMH_TO_MS;
        List<TrajPhase> phases = List.of(
            new TrajPhase(2.0, v0, v0),
            new TrajPhase(3.0, v0, 0.0),
            new TrajPhase(2.0, 0.0, 0.0),
            new TrajPhase(3.0, 0.0, v0),
            new TrajPhase(2.0, v0, v0),
            new TrajPhase(3.0, v0, 0.0),
            new TrajPhase(2.0, 0.0, 0.0),
            new TrajPhase(3.0, 0.0, v0)
        );
        double leadInitialPos = 40.0;
        double ownSpeed = v0;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(leadInitialPos, v0, 0.0, 0.9, true);

        return new Scenario(
            "stop-and-go",
            "Jazda korkowa",
            "Auto z przodu cyklicznie hamuje do zera, stoi 2 s, rusza. Lewy pas zajęty.",
            20.0,
            initial,
            env0,
            (t, own) -> {
                double[] ps = integrate(t, leadInitialPos, phases);

                return new Environment(ps[0], ps[1], 0.0, 0.9, true);
            }
        );
    }

    private Scenario buildMountainRoad() {
        double ownSpeed  = 90 * KMH_TO_MS;
        double leadSpeed = ownSpeed;
        double leadStart = 200.0;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, 0.0, 0.0);
        Environment env0 = new Environment(leadStart, leadSpeed, 0.0, 0.9);

        return new Scenario(
            "mountain-road",
            "Zakręty na górskiej drodze",
            "Pusta droga (90 km/h). Naprzemienne ostre zakręty co 3 s.",
            14.0,
            initial,
            env0,
            (t, own) -> {
                double curve;
                if      (t < 2.0)  curve =  0.0;
                else if (t < 5.0)  curve = -0.03;
                else if (t < 8.0)  curve =  0.03;
                else if (t < 11.0) curve = -0.025;
                else               curve =  0.025;

                return new Environment(leadStart + leadSpeed * t, leadSpeed, curve, 0.9);
            }
        );
    }

    private Scenario buildLaneDrift() {
        double ownSpeed  = 90 * KMH_TO_MS;
        double leadSpeed = ownSpeed;
        double leadStart = 120.0;
        double initialOffset = 1.5;

        VehicleState initial = new VehicleState(0.0, 0.0, ownSpeed, initialOffset, 0.0);
        Environment env0 = new Environment(leadStart, leadSpeed, 0.0, 0.95);

        return new Scenario(
            "lane-drift",
            "Zjazd z pasa i powrót do środka",
            "Start blisko prawej krawędzi pasa. Prosta droga, sucho. Powrót na środek jezdni.",
            8.0,
            initial,
            env0,
            (t, own) -> new Environment(leadStart + leadSpeed * t, leadSpeed, 0.0, 0.95)
        );
    }

    private record TrajPhase(double duration, double startSpeed, double endSpeed) {}

    private static double[] integrate(double t, double startPos, List<TrajPhase> phases) {
        double pos = startPos;
        double tElapsed = 0.0;
        for (TrajPhase ph : phases) {
            if (tElapsed + ph.duration() >= t) {
                double dt = t - tElapsed;
                double speedAtT = ph.startSpeed() + (ph.endSpeed() - ph.startSpeed()) * (dt / ph.duration());
                double avgSpeed = (ph.startSpeed() + speedAtT) / 2.0;

                return new double[] { pos + avgSpeed * dt, speedAtT };
            }

            double avgSpeed = (ph.startSpeed() + ph.endSpeed()) / 2.0;
            pos += avgSpeed * ph.duration();
            tElapsed += ph.duration();
        }

        TrajPhase last = phases.get(phases.size() - 1);

        return new double[] { pos + last.endSpeed() * (t - tElapsed), last.endSpeed() };
    }
}
