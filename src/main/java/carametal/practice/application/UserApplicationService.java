package carametal.practice.application;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final RoleService roleService;
    
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
        userDomainService.publishUserCreatedEvent(savedUser, currentUser.getId());
        
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
        
        // 更新前の値を保存
        Username oldUsername = new Username(existingUser.getUsername());
        Email oldEmail = new Email(existingUser.getEmail());
        Set<String> oldRoleNames = existingUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        
        // ドメインサービスで更新
        userDomainService.updateUser(
                existingUser, newUsername, newEmail, newRoles, currentUser.getId());
        
        User updatedUser = userRepository.save(existingUser);
        
        // ドメインイベント発行
        userDomainService.publishUserUpdatedEvent(
                updatedUser, oldUsername, oldEmail, oldRoleNames, currentUser.getId());
        
        return toUpdateResponse(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // ドメインイベント発行
        userDomainService.publishUserDeletedEvent(user, currentUser.getId());
        
        userRepository.delete(user);
    }
    
    private void validateRoleNames(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Role names are required");
        }
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