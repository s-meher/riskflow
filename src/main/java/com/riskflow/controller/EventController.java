package com.riskflow.controller;

import com.riskflow.dto.EventIngestResponse;
import com.riskflow.dto.PaymentEventIngestRequest;
import com.riskflow.dto.PaymentEventResponse;
import com.riskflow.service.EventIngestionService;
import com.riskflow.service.EventQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final EventQueryService eventQueryService;

    public EventController(EventIngestionService eventIngestionService, EventQueryService eventQueryService) {
        this.eventIngestionService = eventIngestionService;
        this.eventQueryService = eventQueryService;
    }

    @GetMapping
    public List<PaymentEventResponse> list() {
        return eventQueryService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventIngestResponse ingest(@Valid @RequestBody PaymentEventIngestRequest request) {
        return eventIngestionService.ingest(request);
    }
}
