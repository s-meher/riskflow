package com.riskflow.config;

import com.riskflow.service.DemoDataService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "riskflow.demo.seed", havingValue = "true")
public class DemoSeedRunner implements ApplicationRunner {

    private final DemoDataService demoDataService;

    public DemoSeedRunner(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoDataService.seedDemoData();
    }
}

