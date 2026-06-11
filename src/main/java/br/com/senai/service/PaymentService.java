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
import java.util.Optional;

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
    private final NotificationService notificationService;

    public PaymentService(UserService userService,
                          MercadoPagoService mercadoPagoService,
                          PaymentTransactionRepository paymentTransactionRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.userService = userService;
        this.mercadoPagoService = mercadoPagoService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        transaction.setQrCode(result.getQrCode());
        transaction.setType(PaymentType.BUY);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setExpiresAt(result.getExpiresAt() != null
                ? result.getExpiresAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
                : LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        paymentTransactionRepository.save(transaction);

        notificationService.create(
                "Voce tem um pagamento PIX pendente de " + chronosAmount + " Chronos. Conclua o pagamento para receber seus Chronos.",
                user
        );

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

    public Optional<BuyChronosResponseDTO> getPendingBuyPayment(String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);

        return paymentTransactionRepository.findFirstActiveTransaction(
                user.getId(),
                PaymentType.BUY,
                PaymentStatus.PENDING,
                LocalDateTime.now(ZoneOffset.UTC)
        ).map(transaction -> {
            String expiresAtIso = transaction.getExpiresAt()
                    .atOffset(ZoneOffset.UTC).toString();
            return new BuyChronosResponseDTO(
                    transaction.getId(),
                    transaction.getQrCode(),
                    null,
                    expiresAtIso
            );
        });
    }

    @Transactional
    public void handleWebhook(Long mpPaymentId) {
        paymentTransactionRepository.findByMpPaymentId(mpPaymentId).ifPresent(transaction -> {
            if (transaction.getStatus() != PaymentStatus.PENDING) return;

            String mpStatus = mercadoPagoService.getPaymentStatus(mpPaymentId);

            if ("approved".equals(mpStatus)) {
                creditChronos(transaction);
                transaction.setStatus(PaymentStatus.PAID);
                userRepository.findById(transaction.getUserId()).ifPresent(user ->
                        notificationService.create(
                                "Pagamento confirmado! " + transaction.getChronosAmount() + " Chronos foram adicionados ao seu saldo.",
                                user
                        )
                );
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
            user.setTimeChronos(user.getTimeChronos() + chronosAmount);
            userRepository.save(user);
            throw new RuntimeException("Falha ao processar pagamento PIX: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void simulatePaymentApproval(Long transactionId, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);

        PaymentTransactionEntity transaction = paymentTransactionRepository
                .findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Transacao nao encontrada."));

        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Transacao nao esta pendente.");
        }

        creditChronos(transaction);
        transaction.setStatus(PaymentStatus.PAID);
        paymentTransactionRepository.save(transaction);

        notificationService.create(
                "Pagamento simulado! " + transaction.getChronosAmount() + " Chronos foram adicionados ao seu saldo.",
                user
        );
    }

    private void creditChronos(PaymentTransactionEntity transaction) {
        userRepository.findById(transaction.getUserId()).ifPresent(user -> {
            int newBalance = Math.min(user.getTimeChronos() + transaction.getChronosAmount(), MAX_CHRONOS);
            user.setTimeChronos(newBalance);
            userRepository.save(user);
        });
    }
}
