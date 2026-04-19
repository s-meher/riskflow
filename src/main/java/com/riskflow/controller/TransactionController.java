package com.riskflow.controller;

import com.riskflow.dto.TransactionResponse;
import com.riskflow.service.TransactionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    public TransactionController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/flagged")
    public List<TransactionResponse> listFlagged() {
        return transactionQueryService.listFlagged();
    }

    @GetMapping
    public List<TransactionResponse> list() {
        return transactionQueryService.listAll();
    }
}
