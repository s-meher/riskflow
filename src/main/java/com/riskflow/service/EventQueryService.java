package com.riskflow.service;

import com.riskflow.dto.PaymentEventResponse;
import com.riskflow.model.PaymentEvent;
import com.riskflow.repository.PaymentEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventQueryService {

    private final PaymentEventRepository paymentEventRepository;

    public EventQueryService(PaymentEventRepository paymentEventRepository) {
        this.paymentEventRepository = paymentEventRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentEventResponse> listAll() {
        return paymentEventRepository.findAllByOrderByEventTimestampDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentEventResponse toResponse(PaymentEvent e) {
        var tx = e.getTransaction();
        return new PaymentEventResponse(
                e.getId(),
                tx.getTransactionId(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getPaymentMethod(),
                e.getEventType(),
                e.getStatus(),
                e.getReason(),
                e.getEventTimestamp(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
