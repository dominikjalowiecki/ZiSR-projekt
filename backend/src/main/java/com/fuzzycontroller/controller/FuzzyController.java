package com.fuzzycontroller.controller;

import com.fuzzycontroller.model.ControlRequest;
import com.fuzzycontroller.model.ControlResponse;
import com.fuzzycontroller.service.FuzzyEngineService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class FuzzyController {

    private final FuzzyEngineService engine;

    public FuzzyController(FuzzyEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/control")
    public ControlResponse control(@RequestBody ControlRequest request) {
        return engine.evaluate(request);
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return engine.describe();
    }
}
