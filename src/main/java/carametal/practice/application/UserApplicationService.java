package carametal.practice.application;

import carametal.practice.domain.event.UserCreatedEvent;
import carametal.practice.domain.event.UserDeletedEvent;
import carametal.practice.domain.event.UserUpdatedEvent;
import carametal.practice.domain.service.UserDomainService;
import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Password;
import carametal.practice.domain.valueobject.Username;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.dto.UserUpdateResponse;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import carametal.practice.service.RoleService;
import carametal.practice.service.UserAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final RoleService roleService;
    private final UserAuditService userAuditService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request, User currentUser) {
        // Value Objects作成（バリデーション含む）
        Username username = new Username(request.getUsername());
        Email email = new Email(request.getEmail());
        Password password = new Password(request.getPassword());
        
        // ロール取得
        validateRoleNames(request.getRoleNames());
        Set<Role> roles = roleService.findRolesByNames(request.getRoleNames());
        
        // ドメインサービスでユーザー作成
        User user = userDomainService.createUser(username, email, password, roles, currentUser.getId());
        User savedUser = userRepository.save(user);
        
        // ドメインイベント発行
        UserCreatedEvent event = userDomainService.createUserCreatedEvent(savedUser, currentUser.getId());
        publishAuditEvent(event);
        
        return toRegistrationResponse(savedUser);
    }
    
    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request, User currentUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Value Objects作成（バリデーション含む）
        Username newUsername = new Username(request.getUsername());
        Email newEmail = new Email(request.getEmail());
        
        // ロール取得
        validateRoleNames(request.getRoleNames());
        Set<Role> newRoles = roleService.findRolesByNames(request.getRoleNames());
        
        // ドメインサービスで更新
        UserUpdatedEvent event = userDomainService.updateUser(
                existingUser, newUsername, newEmail, newRoles, currentUser.getId());
        
        User updatedUser = userRepository.save(existingUser);
        
        // ドメインイベント発行
        publishAuditEvent(event);
        
        return toUpdateResponse(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // ドメインイベント作成
        UserDeletedEvent event = userDomainService.createUserDeletedEvent(user, currentUser.getId());
        
        userRepository.delete(user);
        
        // ドメインイベント発行
        publishAuditEvent(event);
    }
    
    private void validateRoleNames(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Role names are required");
        }
    }
    
    private void publishAuditEvent(UserCreatedEvent event) {
        Map<String, Object> details = new HashMap<>();
        details.put("username", event.getUsername().getValue());
        details.put("email", event.getEmail().getValue());
        details.put("roles", event.getRoleNames());
        
        userAuditService.logUserCreated(event.getCreatedBy(), event.getUserId(), details);
    }
    
    private void publishAuditEvent(UserUpdatedEvent event) {
        Map<String, Object> details = new HashMap<>();
        details.put("oldUsername", event.getOldUsername().getValue());
        details.put("oldEmail", event.getOldEmail().getValue());
        details.put("oldRoles", event.getOldRoleNames());
        details.put("newUsername", event.getNewUsername().getValue());
        details.put("newEmail", event.getNewEmail().getValue());
        details.put("newRoles", event.getNewRoleNames());
        
        userAuditService.logUserUpdated(event.getUpdatedBy(), event.getUserId(), details);
    }
    
    private void publishAuditEvent(UserDeletedEvent event) {
        Map<String, Object> details = new HashMap<>();
        details.put("deletedUsername", event.getUsername().getValue());
        details.put("deletedEmail", event.getEmail().getValue());
        
        userAuditService.logUserDeleted(event.getDeletedBy(), event.getUserId(), details);
    }
    
    private UserRegistrationResponse toRegistrationResponse(User user) {
        return UserRegistrationResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .registrationDate(user.getRegistrationDate())
                .roleNames(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
    
    private UserUpdateResponse toUpdateResponse(User user) {
        return UserUpdateResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .registrationDate(user.getRegistrationDate())
                .lastUpdated(user.getUpdatedAt())
                .roleNames(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
}