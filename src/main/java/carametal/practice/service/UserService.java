package carametal.practice.service;

import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.dto.UserUpdateResponse;
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
    
    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request, User currentUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (!existingUser.getUsername().equals(request.getUsername())) {
            userValidationService.validateUniqueUsername(request.getUsername());
        }
        
        if (!existingUser.getEmail().equals(request.getEmail())) {
            userValidationService.validateUniqueEmail(request.getEmail());
        }
        
        userValidationService.validateRoleNames(request.getRoleNames());
        
        Set<Role> roles = roleService.findRolesByNames(request.getRoleNames());
        
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("oldUsername", existingUser.getUsername());
        oldDetails.put("oldEmail", existingUser.getEmail());
        oldDetails.put("oldRoles", existingUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()));
        
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setRoles(roles);
        existingUser.setUpdatedBy(currentUser.getId());
        
        User updatedUser = userRepository.save(existingUser);
        
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("newUsername", updatedUser.getUsername());
        newDetails.put("newEmail", updatedUser.getEmail());
        newDetails.put("newRoles", updatedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet()));
        newDetails.putAll(oldDetails);
        
        userAuditService.logUserUpdated(currentUser.getId(), updatedUser.getId(), newDetails);
        
        return UserUpdateResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .registrationDate(updatedUser.getRegistrationDate())
                .lastUpdated(updatedUser.getUpdatedAt())
                .roleNames(updatedUser.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
}