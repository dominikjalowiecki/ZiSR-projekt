package com.fuzzycontroller.model;

public record ControlRequest(
        double distance,
        double relativeSpeed,
        double ownSpeed,
        double roadCondition,
        double curvature,
        double lateralOffset
) {
}
