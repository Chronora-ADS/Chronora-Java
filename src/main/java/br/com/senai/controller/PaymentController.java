package br.com.senai.controller;

import br.com.senai.model.DTO.payment.BuyChronosRequestDTO;
import br.com.senai.model.DTO.payment.BuyChronosResponseDTO;
import br.com.senai.model.DTO.payment.PaymentStatusResponseDTO;
import br.com.senai.model.DTO.payment.SellChronosRequestDTO;
import br.com.senai.service.payment.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/buy/create")
    public ResponseEntity<BuyChronosResponseDTO> createBuyPayment(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody @Valid BuyChronosRequestDTO request
    ) {
        BuyChronosResponseDTO response = paymentService.createBuyPayment(tokenHeader, request.chronosAmount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buy/pending")
    public ResponseEntity<BuyChronosResponseDTO> getPendingBuyPayment(
            @RequestHeader("Authorization") String tokenHeader
    ) {
        return paymentService.getPendingBuyPayment(tokenHeader)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/buy/status/{transactionId}")
    public ResponseEntity<PaymentStatusResponseDTO> getBuyStatus(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long transactionId
    ) {
        String status = paymentService.getBuyPaymentStatus(transactionId, tokenHeader);
        return ResponseEntity.ok(new PaymentStatusResponseDTO(status));
    }

    @PostMapping("/sell/create")
    public ResponseEntity<Void> createSellPayment(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody @Valid SellChronosRequestDTO request
    ) {
        paymentService.createSellPayment(tokenHeader, request.chronosAmount(), request.pixKey());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/approve/{transactionId}")
    public ResponseEntity<Void> simulateApproval(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long transactionId
    ) {
        paymentService.simulatePaymentApproval(transactionId, tokenHeader);
        return ResponseEntity.ok().build();
    }

    // Endpoint chamado pelo Mercado Pago quando um pagamento é confirmado
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String rawBody) {
        try {
            JsonNode node = objectMapper.readTree(rawBody);
            String type = node.path("type").asText();

            if ("payment".equals(type)) {
                Long mpPaymentId = node.path("data").path("id").asLong();
                logger.info("Webhook MP recebido para payment_id={}", mpPaymentId);
                paymentService.handleWebhook(mpPaymentId);
            }
        } catch (Exception e) {
            logger.error("Erro ao processar webhook MP: {}", e.getMessage());
        }
        // Sempre retorna 200 para o MP não retentar desnecessariamente
        return ResponseEntity.ok().build();
    }
}
