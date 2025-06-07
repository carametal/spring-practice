package carametal.practice.service;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        Role empRole = Role.builder()
                .roleName("EMPLOYEE")
                .description("従業員")
                .build();
        empRole.setCreatedBy(1L);
        empRole.setUpdatedBy(1L);
        employeeRole = roleRepository.save(empRole);

        Role sysAdminRole = Role.builder()
                .roleName("SYSTEM_ADMIN")
                .description("システム管理者")
                .build();
        sysAdminRole.setCreatedBy(1L);
        sysAdminRole.setUpdatedBy(1L);
        adminRole = roleRepository.save(sysAdminRole);
    }

    @Test
    void registerUser_正常ケース_デフォルトロール() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        UserRegistrationResponse response = userService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRoleNames()).containsExactly("EMPLOYEE");
        assertThat(response.getRegistrationDate()).isNotNull();

        User savedUser = userRepository.findById(response.getId()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getRoleName()).isEqualTo("EMPLOYEE");
    }

    @Test
    void registerUser_正常ケース_指定ロール() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("password123")
                .roleNames(Set.of("SYSTEM_ADMIN"))
                .build();

        UserRegistrationResponse response = userService.registerUser(request);

        assertThat(response.getRoleNames()).containsExactly("SYSTEM_ADMIN");

        User savedUser = userRepository.findById(response.getId()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getRoleName()).isEqualTo("SYSTEM_ADMIN");
    }

    @Test
    void registerUser_メール重複エラー() {
        User existingUser = User.builder()
                .username("existing")
                .email("test@example.com")
                .password("password")
                .build();
        existingUser.setCreatedBy(1L);
        existingUser.setUpdatedBy(1L);
        userRepository.save(existingUser);

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists: test@example.com");
    }

    @Test
    void registerUser_ユーザー名重複エラー() {
        User existingUser = User.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("password")
                .build();
        existingUser.setCreatedBy(1L);
        existingUser.setUpdatedBy(1L);
        userRepository.save(existingUser);

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists: testuser");
    }
}