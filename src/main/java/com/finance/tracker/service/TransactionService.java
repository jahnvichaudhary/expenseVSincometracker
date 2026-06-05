package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionDtos.*;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.TransactionType;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ApiException;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Response create(String email, CreateRequest req) {
        User u = currentUser(email);
        Transaction t = Transaction.builder()
                .user(u)
                .type(req.type())
                .category(req.category())
                .description(req.description())
                .amount(req.amount())
                .date(req.date())
                .build();
        return toDto(transactionRepository.save(t));
    }

    @Transactional(readOnly = true)
    public List<Response> list(String email) {
        return transactionRepository.findByUserOrderByDateDesc(currentUser(email))
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void delete(String email, Long id) {
        User u = currentUser(email);
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
        if (!t.getUser().getId().equals(u.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your transaction");
        }
        transactionRepository.delete(t);
    }

    @Transactional(readOnly = true)
    public Summary summary(String email) {
        User u = currentUser(email);
        BigDecimal income = transactionRepository.sumByUserAndType(u, TransactionType.INCOME);
        BigDecimal expense = transactionRepository.sumByUserAndType(u, TransactionType.EXPENSE);
        return new Summary(income, expense, income.subtract(expense));
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Response toDto(Transaction t) {
        return new Response(t.getId(), t.getType(), t.getCategory(),
                t.getDescription(), t.getAmount(), t.getDate());
    }
}
