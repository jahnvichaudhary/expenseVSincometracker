package com.finance.tracker.dto;

import com.finance.tracker.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDtos {

    public record CreateRequest(
            @NotNull TransactionType type,
            @NotBlank @Size(max = 60) String category,
            @Size(max = 255) String description,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotNull LocalDate date
    ) {}

    public record Response(
            Long id,
            TransactionType type,
            String category,
            String description,
            BigDecimal amount,
            LocalDate date
    ) {}

    public record Summary(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance
    ) {}
}
