package carametal.practice.domain.service;

import carametal.practice.domain.event.UserCreatedEvent;
import carametal.practice.domain.event.UserDeletedEvent;
import carametal.practice.domain.event.UserUpdatedEvent;
import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Password;
import carametal.practice.domain.valueobject.Username;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    private final UserUniquenessChecker uniquenessChecker;
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(Username username, Email email, Password password, 
                          Set<Role> roles, Long createdBy) {
        validateUniqueConstraints(username, email);
        
        User user = User.builder()
                .username(username.getValue())
                .email(email.getValue())
                .password(passwordEncoder.encode(password.getRawValue()))
                .registrationDate(LocalDateTime.now())
                .roles(roles)
                .build();
        
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);
        
        return user;
    }
    
    public UserCreatedEvent createUserCreatedEvent(User user, Long createdBy) {
        return UserCreatedEvent.builder()
                .userId(user.getId())
                .createdBy(createdBy)
                .username(new Username(user.getUsername()))
                .email(new Email(user.getEmail()))
                .roleNames(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .occurredAt(LocalDateTime.now())
                .build();
    }
    
    public UserUpdatedEvent updateUser(User existingUser, Username newUsername, 
                                     Email newEmail, Set<Role> newRoles, Long updatedBy) {
        Username oldUsername = new Username(existingUser.getUsername());
        Email oldEmail = new Email(existingUser.getEmail());
        Set<String> oldRoleNames = existingUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        
        validateUniqueConstraintsForUpdate(existingUser.getId(), newUsername, newEmail);
        
        existingUser.setUsername(newUsername.getValue());
        existingUser.setEmail(newEmail.getValue());
        existingUser.setRoles(newRoles);
        existingUser.setUpdatedBy(updatedBy);
        
        return UserUpdatedEvent.builder()
                .userId(existingUser.getId())
                .updatedBy(updatedBy)
                .oldUsername(oldUsername)
                .newUsername(newUsername)
                .oldEmail(oldEmail)
                .newEmail(newEmail)
                .oldRoleNames(oldRoleNames)
                .newRoleNames(newRoles.stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .occurredAt(LocalDateTime.now())
                .build();
    }
    
    public UserDeletedEvent createUserDeletedEvent(User user, Long deletedBy) {
        return UserDeletedEvent.builder()
                .userId(user.getId())
                .deletedBy(deletedBy)
                .username(new Username(user.getUsername()))
                .email(new Email(user.getEmail()))
                .occurredAt(LocalDateTime.now())
                .build();
    }
    
    private void validateUniqueConstraints(Username username, Email email) {
        if (!uniquenessChecker.isUsernameUnique(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (!uniquenessChecker.isEmailUnique(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
    
    private void validateUniqueConstraintsForUpdate(Long userId, Username username, Email email) {
        if (!uniquenessChecker.isUsernameUniqueExcluding(username, userId)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (!uniquenessChecker.isEmailUniqueExcluding(email, userId)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
}