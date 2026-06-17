package br.com.senai.service.payment;

import br.com.senai.model.DTO.payment.PaymentTransactionSummaryDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.PaymentTransactionRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.user.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ModeratorService {

    private final PaymentTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ModeratorService(PaymentTransactionRepository transactionRepository,
                            UserRepository userRepository,
                            UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<PaymentTransactionSummaryDTO> getAllTransactions(String tokenHeader) {
        UserEntity requester = userService.getLoggedUser(tokenHeader);
        if (requester.getRoles() == null || !requester.getRoles().contains("ROLE_MODERATOR")) {
            throw new RuntimeException("Acesso negado.");
        }

        var transactions = transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        Map<Long, String> userNames = userRepository.findAllById(
                transactions.stream().map(t -> t.getUserId()).distinct().toList()
        ).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return transactions.stream()
                .map(t -> PaymentTransactionSummaryDTO.from(t, userNames.getOrDefault(t.getUserId(), "Desconhecido")))
                .toList();
    }
}
