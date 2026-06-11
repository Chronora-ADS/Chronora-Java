package br.com.senai.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            logger.warn("MERCADO_PAGO_ACCESS_TOKEN nao configurado — chamadas ao MP vao falhar");
        }
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public PixPaymentResult createPixPayment(BigDecimal amount, String description, String payerEmail) {
        try {
            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .description(description)
                    .paymentMethodId("pix")
                    .dateOfExpiration(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5))
                    .payer(PaymentPayerRequest.builder()
                            .email(payerEmail)
                            .build())
                    .build();

            PaymentClient client = new PaymentClient();
            Payment payment = client.create(request);

            String qrCode = payment.getPointOfInteraction()
                    .getTransactionData().getQrCode();
            String qrCodeBase64 = payment.getPointOfInteraction()
                    .getTransactionData().getQrCodeBase64();

            return new PixPaymentResult(
                    payment.getId(),
                    qrCode,
                    qrCodeBase64,
                    payment.getDateOfExpiration()
            );
        } catch (MPApiException e) {
            String detail = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            logger.error("MP API error ao criar PIX: status={}, body={}", e.getStatusCode(), detail);
            throw new RuntimeException("Erro ao criar pagamento PIX: " + detail, e);
        } catch (MPException e) {
            logger.error("MP error ao criar PIX: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar pagamento PIX: " + e.getMessage(), e);
        }
    }

    public PixPaymentResult sendPixPayout(BigDecimal amount, String pixKey, String payerEmail) {
        try {
            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .description("Venda de Chronos")
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(payerEmail)
                            .build())
                    .build();

            PaymentClient client = new PaymentClient();
            Payment payment = client.create(request);

            return new PixPaymentResult(
                    payment.getId(),
                    null,
                    null,
                    null
            );
        } catch (MPApiException e) {
            String detail = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            logger.error("MP API error ao enviar PIX payout: status={}, body={}", e.getStatusCode(), detail);
            throw new RuntimeException("Erro ao enviar PIX: " + detail, e);
        } catch (MPException e) {
            logger.error("MP error ao enviar PIX payout: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar PIX: " + e.getMessage(), e);
        }
    }

    public String getPaymentStatus(Long mpPaymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(mpPaymentId);
            return payment.getStatus();
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Erro ao consultar pagamento no Mercado Pago: " + e.getMessage(), e);
        }
    }

    @Data
    public static class PixPaymentResult {
        private final Long mpPaymentId;
        private final String qrCode;
        private final String qrCodeBase64;
        private final OffsetDateTime expiresAt;
    }
}
