package com.fuzzycontroller.controller;

import com.fuzzycontroller.model.ControlRequest;
import com.fuzzycontroller.model.ControlResponse;
import com.fuzzycontroller.model.ScenarioDescriptor;
import com.fuzzycontroller.model.SimulationRequest;
import com.fuzzycontroller.model.SimulationSnapshot;
import com.fuzzycontroller.service.FuzzyEngineService;
import com.fuzzycontroller.service.ScenarioService;
import com.fuzzycontroller.service.SimulationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class FuzzyController {

    private final FuzzyEngineService engine;
    private final ScenarioService scenarioService;
    private final SimulationService simulationService;

    public FuzzyController(FuzzyEngineService engine,
                           ScenarioService scenarioService,
                           SimulationService simulationService) {
        this.engine = engine;
        this.scenarioService = scenarioService;
        this.simulationService = simulationService;
    }

    @PostMapping("/control")
    public ControlResponse control(@RequestBody ControlRequest request) {
        return engine.evaluate(request);
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return engine.describe();
    }

    @GetMapping("/scenarios")
    public List<ScenarioDescriptor> scenarios() {
        return scenarioService.list();
    }

    @PostMapping("/simulation/run")
    public List<SimulationSnapshot> run(@RequestBody SimulationRequest request) {
        return simulationService.run(request.scenario(), request.duration());
    }
}
