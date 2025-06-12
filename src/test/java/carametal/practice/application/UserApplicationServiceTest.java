package carametal.practice.application;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.dto.UserUpdateResponse;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Sql("/test-data.sql")
class UserApplicationServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void registerUser_正常ケース() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When
        UserRegistrationResponse response = userApplicationService.registerUser(request, currentUser);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals(Set.of("EMPLOYEE"), response.getRoleNames());
        assertNotNull(response.getRegistrationDate());
        
        // データベースに保存されていることを確認
        User savedUser = userRepository.findById(response.getId()).orElseThrow();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2a$")); // BCrypt hash
    }
    
    @Test
    void registerUser_無効なユーザー名でエラー() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("ab") // 3文字未満
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.registerUser(request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Username must be between 3 and 50 characters"));
    }
    
    @Test
    void registerUser_無効なメールでエラー() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("invalid-email") // 無効なメール形式
                .password("password123")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.registerUser(request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }
    
    @Test
    void registerUser_短いパスワードでエラー() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("12345") // 6文字未満
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.registerUser(request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"));
    }
    
    @Test
    void registerUser_重複ユーザー名でエラー() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testadmin") // 既存のユーザー名
                .email("new@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.registerUser(request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    void updateUser_正常ケース() {
        // Given
        User targetUser = userRepository.findByUsername("employee").orElseThrow();
        Long targetUserId = targetUser.getId();
        
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_employee")
                .email("updated@example.com")
                .roleNames(Set.of("USER_ADMIN"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When
        UserUpdateResponse response = userApplicationService.updateUser(targetUserId, request, currentUser);
        
        // Then
        assertNotNull(response);
        assertEquals(targetUserId, response.getId());
        assertEquals("updated_employee", response.getUsername());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals(Set.of("USER_ADMIN"), response.getRoleNames());
        assertNotNull(response.getLastUpdated());
        
        // データベースが更新されていることを確認
        User updatedUser = userRepository.findById(targetUserId).orElseThrow();
        assertEquals("updated_employee", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }
    
    @Test
    void updateUser_存在しないユーザーでエラー() {
        // Given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.updateUser(999L, request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
    }
    
    @Test
    void updateUser_重複ユーザー名でエラー() {
        // Given
        User targetUser = userRepository.findByUsername("employee").orElseThrow();
        Long targetUserId = targetUser.getId();
        
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("testadmin") // 既存の他のユーザー名
                .email("new@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.updateUser(targetUserId, request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    void deleteUser_正常ケース() {
        // Given
        User targetUser = userRepository.findByUsername("employee").orElseThrow();
        Long targetUserId = targetUser.getId();
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When
        userApplicationService.deleteUser(targetUserId, currentUser);
        
        // Then
        assertFalse(userRepository.existsById(targetUserId));
    }
    
    @Test
    void deleteUser_存在しないユーザーでエラー() {
        // Given
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.deleteUser(999L, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
    }
    
    @Test
    void registerUser_空のロール名でエラー() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of()) // 空のロール
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userApplicationService.registerUser(request, currentUser)
        );
        
        assertTrue(exception.getMessage().contains("Role names are required"));
    }
}