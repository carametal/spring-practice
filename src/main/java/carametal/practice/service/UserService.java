package carametal.practice.service;

import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        
        Set<Role> roles = new HashSet<>();
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            roles = roleRepository.findByRoleNameIn(request.getRoleNames());
        } else {
            roleRepository.findByRoleName("EMPLOYEE").ifPresent(roles::add);
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .registrationDate(LocalDateTime.now())
                .roles(roles)
                .build();
        
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        
        User savedUser = userRepository.save(user);
        
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