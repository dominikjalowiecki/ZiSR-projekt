package com.fuzzycontroller.model;

public record Environment(
    double leadPosition,
    double leadSpeed,
    double curvature,
    double roadCondition,
    boolean leftLaneBlocked
) {
    public Environment(double leadPosition, double leadSpeed, double curvature, double roadCondition) {
        this(leadPosition, leadSpeed, curvature, roadCondition, false);
    }
}
