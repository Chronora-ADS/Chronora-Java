package br.com.senai.service;

import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.BuyChronosResponseDTO;
import br.com.senai.model.entity.PaymentTransactionEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.PaymentStatus;
import br.com.senai.model.enums.PaymentType;
import br.com.senai.repository.PaymentTransactionRepository;
import br.com.senai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class PaymentService {

    private static final BigDecimal CHRONOS_BUY_PRICE = new BigDecimal("2.50");
    private static final BigDecimal CHRONOS_SELL_PRICE = new BigDecimal("2.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final int MAX_CHRONOS = 300;

    private final UserService userService;
    private final MercadoPagoService mercadoPagoService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;

    public PaymentService(UserService userService,
                          MercadoPagoService mercadoPagoService,
                          PaymentTransactionRepository paymentTransactionRepository,
                          UserRepository userRepository) {
        this.userService = userService;
        this.mercadoPagoService = mercadoPagoService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BuyChronosResponseDTO createBuyPayment(String tokenHeader, Integer chronosAmount) {
        UserEntity user = userService.getLoggedUser(tokenHeader);

        if (user.getTimeChronos() + chronosAmount > MAX_CHRONOS) {
            throw new QuantityChronosInvalidException(
                    "Limite de " + MAX_CHRONOS + " Chronos por usuario seria excedido.");
        }

        BigDecimal subtotal = CHRONOS_BUY_PRICE.multiply(BigDecimal.valueOf(chronosAmount));
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        MercadoPagoService.PixPaymentResult result = mercadoPagoService.createPixPayment(
                total,
                chronosAmount + " Chronos - Chronora",
                user.getEmail()
        );

        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setUserId(user.getId());
        transaction.setChronosAmount(chronosAmount);
        transaction.setTotalAmount(total);
        transaction.setMpPaymentId(result.getMpPaymentId());
        transaction.setType(PaymentType.BUY);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setExpiresAt(result.getExpiresAt() != null
                ? result.getExpiresAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
                : LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30));
        paymentTransactionRepository.save(transaction);

        String expiresAtIso = transaction.getExpiresAt()
                .atOffset(ZoneOffset.UTC).toString();

        return new BuyChronosResponseDTO(
                transaction.getId(),
                result.getQrCode(),
                result.getQrCodeBase64(),
                expiresAtIso
        );
    }

    public String getBuyPaymentStatus(Long transactionId, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Transacao nao encontrada."));

        return transaction.getStatus().name();
    }

    @Transactional
    public void handleWebhook(Long mpPaymentId) {
        paymentTransactionRepository.findByMpPaymentId(mpPaymentId).ifPresent(transaction -> {
            if (transaction.getStatus() != PaymentStatus.PENDING) return;

            String mpStatus = mercadoPagoService.getPaymentStatus(mpPaymentId);

            if ("approved".equals(mpStatus)) {
                creditChronos(transaction);
                transaction.setStatus(PaymentStatus.PAID);
            } else if ("rejected".equals(mpStatus) || "cancelled".equals(mpStatus)) {
                transaction.setStatus(PaymentStatus.FAILED);
            }

            paymentTransactionRepository.save(transaction);
        });
    }

    @Transactional
    public void createSellPayment(String tokenHeader, Integer chronosAmount, String pixKey) {
        UserEntity user = userService.getLoggedUser(tokenHeader);

        if (user.getTimeChronos() - chronosAmount < 0) {
            throw new QuantityChronosInvalidException("Saldo insuficiente de Chronos.");
        }

        BigDecimal subtotal = CHRONOS_SELL_PRICE.multiply(BigDecimal.valueOf(chronosAmount));
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountToReceive = subtotal.subtract(tax);

        // Debita os Chronos antes de tentar o pagamento
        user.setTimeChronos(user.getTimeChronos() - chronosAmount);
        userRepository.save(user);

        try {
            MercadoPagoService.PixPaymentResult result = mercadoPagoService.sendPixPayout(
                    amountToReceive,
                    pixKey,
                    user.getEmail()
            );

            PaymentTransactionEntity transaction = new PaymentTransactionEntity();
            transaction.setUserId(user.getId());
            transaction.setChronosAmount(chronosAmount);
            transaction.setTotalAmount(amountToReceive);
            transaction.setMpPaymentId(result.getMpPaymentId());
            transaction.setPixKey(pixKey);
            transaction.setType(PaymentType.SELL);
            transaction.setStatus(PaymentStatus.PAID);
            transaction.setCreatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

        } catch (Exception e) {
            // Reverte o débito se o MP falhar
            user.setTimeChronos(user.getTimeChronos() + chronosAmount);
            userRepository.save(user);
            throw new RuntimeException("Falha ao processar pagamento PIX: " + e.getMessage(), e);
        }
    }

    private void creditChronos(PaymentTransactionEntity transaction) {
        userRepository.findById(transaction.getUserId()).ifPresent(user -> {
            int newBalance = Math.min(user.getTimeChronos() + transaction.getChronosAmount(), MAX_CHRONOS);
            user.setTimeChronos(newBalance);
            userRepository.save(user);
        });
    }
}
