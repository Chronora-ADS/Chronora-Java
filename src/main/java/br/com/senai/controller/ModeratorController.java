package br.com.senai.controller;

import br.com.senai.model.DTO.payment.PaymentTransactionSummaryDTO;
import br.com.senai.model.DTO.payment.PlatformStatsDTO;
import br.com.senai.model.DTO.service.ModeratorServiceSummaryDTO;
import br.com.senai.model.DTO.user.UserResponseDTO;
import br.com.senai.model.enums.PaymentType;
import br.com.senai.service.payment.ModeratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/transactions/buy")
    public ResponseEntity<List<PaymentTransactionSummaryDTO>> getBuyTransactions(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getTransactionsByType(tokenHeader, PaymentType.BUY));
    }

    @GetMapping("/transactions/sell")
    public ResponseEntity<List<PaymentTransactionSummaryDTO>> getSellTransactions(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getTransactionsByType(tokenHeader, PaymentType.SELL));
    }

    @PatchMapping("/sell/{id}/mark-paid")
    public ResponseEntity<Void> markSellAsPaid(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long id
    ) {
        moderatorService.markSellAsPaid(tokenHeader, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<PlatformStatsDTO> getStats(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getStats(tokenHeader));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getAllUsers(tokenHeader));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ModeratorServiceSummaryDTO>> getAllServices(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return ResponseEntity.ok(moderatorService.getAllServices(tokenHeader));
    }
}
