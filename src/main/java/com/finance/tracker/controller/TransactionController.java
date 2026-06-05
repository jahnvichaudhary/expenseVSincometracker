package com.finance.tracker.controller;

import com.finance.tracker.dto.TransactionDtos.*;
import com.finance.tracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping
    public List<Response> list(@AuthenticationPrincipal UserDetails me) {
        return service.list(me.getUsername());
    }

    @PostMapping
    public Response create(@AuthenticationPrincipal UserDetails me,
                           @Valid @RequestBody CreateRequest req) {
        return service.create(me.getUsername(), req);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserDetails me, @PathVariable Long id) {
        service.delete(me.getUsername(), id);
    }

    @GetMapping("/summary")
    public Summary summary(@AuthenticationPrincipal UserDetails me) {
        return service.summary(me.getUsername());
    }
}
