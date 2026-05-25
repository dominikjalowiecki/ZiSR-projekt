package com.fuzzycontroller.model;

public record VehicleState(
    double time,
    double position,
    double speed,
    double lateralOffset,
    double lane
) {
}
