package com.finance.tracker.controller;

import com.finance.tracker.entity.User;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return userRepository.findAll().stream()
                .map(this::view)
                .toList();
    }

    private Map<String, Object> view(User u) {
        return Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "roles", u.getRoles(),
                "createdAt", u.getCreatedAt()
        );
    }
}
