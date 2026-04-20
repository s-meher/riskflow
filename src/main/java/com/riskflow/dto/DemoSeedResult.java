package com.riskflow.dto;

import java.util.List;

public record DemoSeedResult(
        boolean seeded,
        int eventsAttempted,
        int eventsCreated,
        int transactionsTotal,
        List<String> notes
) {
}

