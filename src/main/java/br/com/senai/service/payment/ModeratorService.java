package br.com.senai.service.payment;

import br.com.senai.model.DTO.payment.PaymentTransactionSummaryDTO;
import br.com.senai.model.DTO.payment.PlatformStatsDTO;
import br.com.senai.model.DTO.service.ModeratorServiceSummaryDTO;
import br.com.senai.model.DTO.user.UserResponseDTO;
import br.com.senai.model.entity.PaymentTransactionEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.PaymentStatus;
import br.com.senai.model.enums.PaymentType;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.PaymentTransactionRepository;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.user.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModeratorService {

    private final PaymentTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final UserService userService;

    public ModeratorService(PaymentTransactionRepository transactionRepository,
                            UserRepository userRepository,
                            ServiceRepository serviceRepository,
                            UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.userService = userService;
    }

    public List<PaymentTransactionSummaryDTO> getAllTransactions(String tokenHeader) {
        requireModerator(tokenHeader);

        var transactions = transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        Map<Long, String> userNames = userRepository.findAllById(
                transactions.stream().map(t -> t.getUserId()).distinct().toList()
        ).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return transactions.stream()
                .map(t -> PaymentTransactionSummaryDTO.from(t, userNames.getOrDefault(t.getUserId(), "Desconhecido")))
                .toList();
    }

    public List<PaymentTransactionSummaryDTO> getTransactionsByType(String tokenHeader, PaymentType type) {
        requireModerator(tokenHeader);

        var transactions = transactionRepository.findAllByTypeOrderByCreatedAtDesc(type);

        Map<Long, String> userNames = userRepository.findAllById(
                transactions.stream().map(t -> t.getUserId()).distinct().toList()
        ).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return transactions.stream()
                .map(t -> PaymentTransactionSummaryDTO.from(t, userNames.getOrDefault(t.getUserId(), "Desconhecido")))
                .toList();
    }

    @Transactional
    public void markSellAsPaid(String tokenHeader, Long transactionId) {
        requireModerator(tokenHeader);

        PaymentTransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacao nao encontrada."));

        if (transaction.getType() != PaymentType.SELL) {
            throw new RuntimeException("Transacao nao e uma venda.");
        }

        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Apenas transacoes pendentes podem ser marcadas como pagas.");
        }

        transaction.setStatus(PaymentStatus.PAID);
        transactionRepository.save(transaction);
    }

    public PlatformStatsDTO getStats(String tokenHeader) {
        requireModerator(tokenHeader);

        long totalUsuarios = userRepository.count();

        long totalPedidos      = serviceRepository.count();
        long pedidosCriados    = serviceRepository.countByStatus(ServiceStatus.CRIADO);
        long pedidosEmAndamento= serviceRepository.countByStatus(ServiceStatus.EM_ANDAMENTO);
        long pedidosConcluidos = serviceRepository.countByStatus(ServiceStatus.CONCLUIDO);
        long pedidosCancelados = serviceRepository.countByStatus(ServiceStatus.CANCELADO);

        long totalTransacoes    = transactionRepository.count();
        long transacoesPagas    = transactionRepository.countByStatus(PaymentStatus.PAID);
        long transacoesPendentes= transactionRepository.countByStatus(PaymentStatus.PENDING);
        long transacoesFalhas   = transactionRepository.countByStatus(PaymentStatus.FAILED);

        Long chronosComprados = transactionRepository.sumChronosByTypeAndStatus(PaymentType.BUY, PaymentStatus.PAID);
        Long chronosVendidos  = transactionRepository.sumChronosByTypeAndStatus(PaymentType.SELL, PaymentStatus.PAID);
        BigDecimal volume     = transactionRepository.sumAmountByStatus(PaymentStatus.PAID);

        return new PlatformStatsDTO(
                totalUsuarios,
                totalPedidos,
                pedidosCriados,
                pedidosEmAndamento,
                pedidosConcluidos,
                pedidosCancelados,
                totalTransacoes,
                transacoesPagas,
                transacoesPendentes,
                transacoesFalhas,
                chronosComprados != null ? chronosComprados : 0L,
                chronosVendidos  != null ? chronosVendidos  : 0L,
                volume != null ? volume : BigDecimal.ZERO
        );
    }

    public List<UserResponseDTO> getAllUsers(String tokenHeader) {
        requireModerator(tokenHeader);
        return userRepository.findAllWithRoles()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    public List<ModeratorServiceSummaryDTO> getAllServices(String tokenHeader) {
        requireModerator(tokenHeader);
        return serviceRepository.findAllWithCategoriesOrderByPostedAtDesc()
                .stream()
                .map(ModeratorServiceSummaryDTO::from)
                .toList();
    }

    private void requireModerator(String tokenHeader) {
        UserEntity requester = userService.getLoggedUser(tokenHeader);
        if (requester.getRoles() == null || !requester.getRoles().contains("ROLE_MODERATOR")) {
            throw new RuntimeException("Acesso negado.");
        }
    }
}
