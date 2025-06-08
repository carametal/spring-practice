package carametal.practice.service;

import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAuditService userAuditService;
    private final UserValidationService userValidationService;
    private final RoleService roleService;
    
    @Transactional
    public void deleteUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Map<String, Object> details = new HashMap<>();
        details.put("deletedUsername", user.getUsername());
        details.put("deletedEmail", user.getEmail());
        
        userRepository.delete(user);
        
        userAuditService.logUserDeleted(currentUser.getId(), userId, details);
    }
    
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request, User currentUser) {
        userValidationService.validateUniqueEmail(request.getEmail());
        userValidationService.validateUniqueUsername(request.getUsername());
        userValidationService.validateRoleNames(request.getRoleNames());
        
        Set<Role> roles = roleService.findRolesByNames(request.getRoleNames());
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .registrationDate(LocalDateTime.now())
                .roles(roles)
                .build();
        
        user.setCreatedBy(currentUser.getId());
        user.setUpdatedBy(currentUser.getId());
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> details = new HashMap<>();
        details.put("username", savedUser.getUsername());
        details.put("email", savedUser.getEmail());
        details.put("roles", savedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()));
        
        userAuditService.logUserCreated(currentUser.getId(), savedUser.getId(), details);
        
        return UserRegistrationResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .registrationDate(savedUser.getRegistrationDate())
                .roleNames(savedUser.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
}