package carametal.practice.domain.service;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Password;
import carametal.practice.domain.valueobject.Username;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Sql("/test-data.sql")
class UserDomainServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserDomainService userDomainService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private Role employeeRole;
    
    @BeforeEach
    void setUp() {
        employeeRole = roleRepository.findByRoleName("EMPLOYEE").orElseThrow();
    }
    
    @Test
    void createUser_正常ケース() {
        // Given
        Username username = new Username("newuser");
        Email email = new Email("newuser@example.com");
        Password password = new Password("password123");
        Set<Role> roles = Set.of(employeeRole);
        Long createdBy = 1L;
        
        // When
        User result = userDomainService.createUser(username, email, password, roles, createdBy);
        
        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        assertNotNull(result.getPassword());
        assertTrue(result.getPassword().startsWith("$2a$")); // BCrypt hash
        assertEquals(roles, result.getRoles());
        assertEquals(createdBy, result.getCreatedBy());
        assertEquals(createdBy, result.getUpdatedBy());
        assertNotNull(result.getRegistrationDate());
    }
    
    @Test
    void createUser_ユーザー名重複でエラー() {
        // Given - testadminは既存ユーザー
        Username username = new Username("testadmin");
        Email email = new Email("new@example.com");
        Password password = new Password("password123");
        Set<Role> roles = Set.of(employeeRole);
        Long createdBy = 1L;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userDomainService.createUser(username, email, password, roles, createdBy)
        );
        
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    void createUser_メール重複でエラー() {
        // Given - testadmin@example.comは既存メール
        Username username = new Username("newuser");
        Email email = new Email("testadmin@example.com");
        Password password = new Password("password123");
        Set<Role> roles = Set.of(employeeRole);
        Long createdBy = 1L;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userDomainService.createUser(username, email, password, roles, createdBy)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
    }
    
    @Test
    void publishUserCreatedEvent_正常ケース() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(employeeRole))
                .build();
        Long createdBy = 2L;
        
        // When & Then - イベント発行が正常に動作することを確認（例外が発生しない）
        assertDoesNotThrow(() -> userDomainService.publishUserCreatedEvent(user, createdBy));
    }
    
    @Test
    void updateUser_正常ケース() {
        // Given - 既存のemployeeユーザーを取得
        User existingUser = userRepository.findByUsername("employee").orElseThrow();
        Long originalId = existingUser.getId();
        
        Username newUsername = new Username("updated_employee");
        Email newEmail = new Email("updated@example.com");
        Role userAdminRole = roleRepository.findByRoleName("USER_ADMIN").orElseThrow();
        Set<Role> newRoles = Set.of(userAdminRole);
        Long updatedBy = 2L;
        
        // When
        userDomainService.updateUser(
                existingUser, newUsername, newEmail, newRoles, updatedBy);
        
        // Then
        // ユーザーが更新されていることを確認
        assertEquals("updated_employee", existingUser.getUsername());
        assertEquals("updated@example.com", existingUser.getEmail());
        assertEquals(newRoles, existingUser.getRoles());
        assertEquals(updatedBy, existingUser.getUpdatedBy());
        assertEquals(originalId, existingUser.getId()); // IDは変わらない
    }
    
    @Test
    void updateUser_同じユーザーの更新で重複チェックをスキップ() {
        // Given - 既存のemployeeユーザーを取得
        User existingUser = userRepository.findByUsername("employee").orElseThrow();
        
        // 同じユーザー名・メールで更新（重複チェックをスキップすべき）
        Username sameUsername = new Username(existingUser.getUsername());
        Email sameEmail = new Email(existingUser.getEmail());
        Set<Role> newRoles = Set.of(employeeRole);
        Long updatedBy = 2L;
        
        // When & Then - エラーが発生しないことを確認
        assertDoesNotThrow(() -> userDomainService.updateUser(
                existingUser, sameUsername, sameEmail, newRoles, updatedBy));
    }
    
    @Test
    void updateUser_他のユーザーと重複でエラー() {
        // Given - 既存のemployeeユーザーを取得
        User existingUser = userRepository.findByUsername("employee").orElseThrow();
        
        // testadminのユーザー名に変更しようとする（重複エラーになるべき）
        Username duplicateUsername = new Username("testadmin");
        Email newEmail = new Email("new@example.com");
        Set<Role> newRoles = Set.of(employeeRole);
        Long updatedBy = 2L;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userDomainService.updateUser(existingUser, duplicateUsername, newEmail, newRoles, updatedBy)
        );
        
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    @Test
    void publishUserDeletedEvent_正常ケース() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        Long deletedBy = 2L;
        
        // When & Then - イベント発行が正常に動作することを確認（例外が発生しない）
        assertDoesNotThrow(() -> userDomainService.publishUserDeletedEvent(user, deletedBy));
    }
}