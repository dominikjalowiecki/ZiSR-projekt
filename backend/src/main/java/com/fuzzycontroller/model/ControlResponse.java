package com.fuzzycontroller.model;

import java.util.List;
import java.util.Map;

public record ControlResponse(
        double acceleration,
        double steeringCorrection,
        double laneChangeUrgency,
        String laneChangeAdvice,
        List<RuleActivation> activatedRules,
        Map<String, Map<String, Double>> fuzzifiedInputs
) {
}
