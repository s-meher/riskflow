package com.riskflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "RiskFlow");
        body.put("message", "API is running. Use the paths below (JSON).");
        body.put(
                "paths",
                List.of(
                        "GET  /api/events",
                        "POST /api/events",
                        "GET  /api/transactions",
                        "GET  /api/transactions/flagged",
                        "GET  /api/metrics/summary",
                        "GET  /api/payments",
                        "POST /api/payments"
                )
        );
        return body;
    }
}
