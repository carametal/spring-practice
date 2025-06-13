package carametal.practice.integration;

import carametal.practice.application.UserApplicationService;
import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserRegistrationResponse;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.dto.UserUpdateResponse;
import carametal.practice.entity.User;
import carametal.practice.entity.UserAuditEvent;
import carametal.practice.repository.UserRepository;
import carametal.practice.repository.UserAuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that domain events are properly published and handled
 * by event listeners to create audit log entries.
 */
@Sql("/test-data.sql")
class UserEventIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserApplicationService userApplicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAuditEventRepository auditEventRepository;

    @Test
    void registerUser_イベントが発行されAuditログが作成される() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("password123")
                .roleNames(Set.of("EMPLOYEE", "USER_ADMIN"))
                .build();
        
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        // When
        UserRegistrationResponse response = userApplicationService.registerUser(request, currentUser);
        
        // Then
        // ユーザーが作成されていることを確認
        assertNotNull(response);
        assertNotNull(response.getId());
        
        // Auditログが作成されていることを確認
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(response.getId());
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(currentUser.getId(), auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_CREATED, auditEvent.getAction());
        assertEquals(response.getId(), auditEvent.getTargetUserId());
        
        // Audit詳細の確認
        assertEquals("integrationuser", auditEvent.getDetails().get("username"));
        assertEquals("integration@example.com", auditEvent.getDetails().get("email"));
        assertEquals(Set.of("EMPLOYEE", "USER_ADMIN"), auditEvent.getDetails().get("roles"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }

    @Test
    void updateUser_イベントが発行されAuditログが作成される() {
        // Given
        User existingUser = userRepository.findByUsername("employee").orElseThrow();
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_employee")
                .email("updated@example.com")
                .roleNames(Set.of("USER_ADMIN"))
                .build();
        
        // When
        UserUpdateResponse response = userApplicationService.updateUser(existingUser.getId(), request, currentUser);
        
        // Then
        // ユーザーが更新されていることを確認
        assertNotNull(response);
        assertEquals("updated_employee", response.getUsername());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals(Set.of("USER_ADMIN"), response.getRoleNames());
        
        // Auditログが作成されていることを確認
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(existingUser.getId());
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(currentUser.getId(), auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_UPDATED, auditEvent.getAction());
        assertEquals(existingUser.getId(), auditEvent.getTargetUserId());
        
        // Audit詳細の確認
        assertEquals("employee", auditEvent.getDetails().get("oldUsername"));
        assertEquals("updated_employee", auditEvent.getDetails().get("newUsername"));
        assertEquals("employee@example.com", auditEvent.getDetails().get("oldEmail"));
        assertEquals("updated@example.com", auditEvent.getDetails().get("newEmail"));
        assertEquals(Set.of("EMPLOYEE"), auditEvent.getDetails().get("oldRoles"));
        assertEquals(Set.of("USER_ADMIN"), auditEvent.getDetails().get("newRoles"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }

    @Test
    void deleteUser_イベントが発行されAuditログが作成される() {
        // Given
        User existingUser = userRepository.findByUsername("employee").orElseThrow();
        User currentUser = userRepository.findByUsername("testadmin").orElseThrow();
        Long targetUserId = existingUser.getId();
        
        // When
        userApplicationService.deleteUser(targetUserId, currentUser);
        
        // Then
        // ユーザーが削除されていることを確認
        assertFalse(userRepository.existsById(targetUserId));
        
        // Auditログが作成されていることを確認
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(targetUserId);
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(currentUser.getId(), auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_DELETED, auditEvent.getAction());
        assertEquals(targetUserId, auditEvent.getTargetUserId());
        
        // Audit詳細の確認
        assertEquals("employee", auditEvent.getDetails().get("username"));
        assertEquals("employee@example.com", auditEvent.getDetails().get("email"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }
}