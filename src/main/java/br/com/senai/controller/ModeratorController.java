package br.com.senai.controller;

import br.com.senai.model.DTO.payment.PaymentTransactionSummaryDTO;
import br.com.senai.service.payment.ModeratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/moderator")
public class ModeratorController {

    private final ModeratorService moderatorService;

    public ModeratorController(ModeratorService moderatorService) {
        this.moderatorService = moderatorService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentTransactionSummaryDTO>> getAllTransactions(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getAllTransactions(tokenHeader));
    }
}
