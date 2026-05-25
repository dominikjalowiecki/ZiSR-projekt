package com.fuzzycontroller.service;

import com.fuzzycontroller.model.Environment;
import com.fuzzycontroller.model.VehicleState;

import java.util.function.BiFunction;

public record Scenario(
    String id,
    String label,
    String description,
    double defaultDuration,
    VehicleState initialVehicle,
    Environment initialEnvironment,
    BiFunction<Double, VehicleState, Environment> environmentAt
) {
    public Environment environmentAt(double time, VehicleState own) {
        return environmentAt.apply(time, own);
    }
}
