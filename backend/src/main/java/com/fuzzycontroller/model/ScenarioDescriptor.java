package com.fuzzycontroller.model;

public record ScenarioDescriptor(
    String id,
    String label,
    String description,
    double defaultDuration
) {
}
