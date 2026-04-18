package com.riskflow.controller;

import com.riskflow.dto.PaymentRequest;
import com.riskflow.dto.PaymentResponse;
import com.riskflow.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<PaymentResponse> list() {
        return paymentService.listPayments();
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable UUID id) {
        return paymentService.getPayment(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse ingest(@Valid @RequestBody PaymentRequest request) {
        return paymentService.ingest(request);
    }
}
