package carametal.practice.service;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql("/test-data.sql")
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUser_正常ケース_デフォルトロール() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        UserRegistrationResponse response = userService.registerUser(request, currentUser);

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

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        UserRegistrationResponse response = userService.registerUser(request, currentUser);

        assertThat(response.getRoleNames()).containsExactly("SYSTEM_ADMIN");

        User savedUser = userRepository.findById(response.getId()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getRoleName()).isEqualTo("SYSTEM_ADMIN");
    }

    @Test
    void registerUser_メール重複エラー() {

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("testadmin@example.com")
                .password("password123")
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        assertThatThrownBy(() -> userService.registerUser(request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists: testadmin@example.com");
    }

    @Test
    void registerUser_ユーザー名重複エラー() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testadmin")
                .email("test@example.com")
                .password("password123")
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        assertThatThrownBy(() -> userService.registerUser(request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists: testadmin");
    }

    @Test
    void registerUser_存在しないロール名指定() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("INVALID_ROLE"))
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        assertThatThrownBy(() -> userService.registerUser(request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid roles: [INVALID_ROLE]");
    }

    @Test
    void registerUser_一部存在しないロール名指定() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE", "INVALID_ROLE"))
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        assertThatThrownBy(() -> userService.registerUser(request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid roles: [INVALID_ROLE]");
    }

    @Test
    void registerUser_パスワード暗号化検証() {
        String rawPassword = "password123";
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password(rawPassword)
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        UserRegistrationResponse response = userService.registerUser(request, currentUser);

        User savedUser = userRepository.findById(response.getId()).orElse(null);
        assertThat(savedUser).isNotNull();

        // パスワードが暗号化されていることを確認
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(savedUser.getPassword()).isNotNull();
        assertThat(savedUser.getPassword()).isNotEmpty();

        // 暗号化されたパスワードが元のパスワードと一致することを確認
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();

        // 間違ったパスワードでは一致しないことを確認
        assertThat(passwordEncoder.matches("wrongpassword", savedUser.getPassword())).isFalse();
    }

    @Test
    void deleteUser_正常ケース() {
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        User targetUser = userRepository.findByUsername("employee").orElseThrow();
        Long targetUserId = targetUser.getId();

        userService.deleteUser(targetUserId, currentUser);

        assertThat(userRepository.findById(targetUserId)).isEmpty();
    }

    @Test
    void deleteUser_存在しないユーザー() {
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> userService.deleteUser(nonExistentUserId, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + nonExistentUserId);
    }
}
