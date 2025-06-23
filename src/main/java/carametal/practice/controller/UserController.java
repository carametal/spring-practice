package carametal.practice.controller;

import carametal.practice.annotation.CurrentUser;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.dto.UserUpdateResponse;
import carametal.practice.entity.User;
import carametal.practice.application.UserApplicationService;
import carametal.practice.repository.UserRepository;
import carametal.practice.specification.UserSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER_ADMIN')")
    public ResponseEntity<UserRegistrationResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request,
            @CurrentUser User currentUser) {
        try {
            UserRegistrationResponse response = userApplicationService.registerUser(request, currentUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER_ADMIN')")
    public ResponseEntity<UserUpdateResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser User currentUser) {
        try {
            UserUpdateResponse response = userApplicationService.updateUser(userId, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            @CurrentUser User currentUser) {
        try {
            userApplicationService.deleteUser(userId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER_ADMIN')")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        
        Specification<User> spec = Specification.where(null);
        
        if (username != null && !username.trim().isEmpty()) {
            spec = spec.and(UserSpecification.hasUsernameContaining(username));
        }
        
        if (email != null && !email.trim().isEmpty()) {
            spec = spec.and(UserSpecification.hasEmailContaining(email));
        }
        
        List<User> users = userRepository.findAll(spec);
        return ResponseEntity.ok(users);
    }
}
