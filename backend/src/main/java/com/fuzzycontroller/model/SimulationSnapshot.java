package com.fuzzycontroller.model;

public record SimulationSnapshot(
    double time,
    double ownPosition,
    double ownSpeed,
    double leadPosition,
    double leadSpeed,
    double lateralOffset,
    double lane,
    double curvature,
    double roadCondition,
    double distance,
    double relativeSpeed,
    double acceleration,
    double steeringCorrection,
    double laneChangeUrgency,
    String laneChangeAdvice,
    boolean leftLaneBlocked
) {
}
