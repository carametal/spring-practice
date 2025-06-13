package carametal.practice.infrastructure;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.domain.event.UserCreatedEvent;
import carametal.practice.domain.event.UserDeletedEvent;
import carametal.practice.domain.event.UserUpdatedEvent;
import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;
import carametal.practice.entity.UserAuditEvent;
import carametal.practice.repository.UserAuditEventRepository;
import carametal.practice.service.UserAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Sql("/test-data.sql")
class UserEventListenerTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private UserAuditEventRepository auditEventRepository;
    
    @Autowired
    private UserAuditService userAuditService;

    @Test
    void handleUserCreatedEvent_正常にAuditログが作成される() {
        // Given
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(100L)
                .createdBy(1L)
                .username(new Username("newuser"))
                .email(new Email("newuser@example.com"))
                .roleNames(Set.of("EMPLOYEE", "USER_ADMIN"))
                .occurredAt(LocalDateTime.now())
                .build();

        // When
        eventPublisher.publishEvent(event);

        // Then
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(100L);
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(1L, auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_CREATED, auditEvent.getAction());
        assertEquals(100L, auditEvent.getTargetUserId());
        
        // Detailsの確認
        assertTrue(auditEvent.getDetails().containsKey("username"));
        assertEquals("newuser", auditEvent.getDetails().get("username"));
        assertTrue(auditEvent.getDetails().containsKey("email"));
        assertEquals("newuser@example.com", auditEvent.getDetails().get("email"));
        assertTrue(auditEvent.getDetails().containsKey("roles"));
        assertEquals(Set.of("EMPLOYEE", "USER_ADMIN"), auditEvent.getDetails().get("roles"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }

    @Test
    void handleUserUpdatedEvent_正常にAuditログが作成される() {
        // Given
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(101L)
                .updatedBy(2L)
                .oldUsername(new Username("olduser"))
                .newUsername(new Username("newuser"))
                .oldEmail(new Email("old@example.com"))
                .newEmail(new Email("new@example.com"))
                .oldRoleNames(Set.of("EMPLOYEE"))
                .newRoleNames(Set.of("USER_ADMIN"))
                .occurredAt(LocalDateTime.now())
                .build();

        // When
        eventPublisher.publishEvent(event);

        // Then
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(101L);
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(2L, auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_UPDATED, auditEvent.getAction());
        assertEquals(101L, auditEvent.getTargetUserId());
        
        // Detailsの確認
        assertEquals("olduser", auditEvent.getDetails().get("oldUsername"));
        assertEquals("newuser", auditEvent.getDetails().get("newUsername"));
        assertEquals("old@example.com", auditEvent.getDetails().get("oldEmail"));
        assertEquals("new@example.com", auditEvent.getDetails().get("newEmail"));
        assertEquals(Set.of("EMPLOYEE"), auditEvent.getDetails().get("oldRoles"));
        assertEquals(Set.of("USER_ADMIN"), auditEvent.getDetails().get("newRoles"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }

    @Test
    void handleUserDeletedEvent_正常にAuditログが作成される() {
        // Given
        UserDeletedEvent event = UserDeletedEvent.builder()
                .userId(102L)
                .deletedBy(3L)
                .username(new Username("deleteduser"))
                .email(new Email("deleted@example.com"))
                .occurredAt(LocalDateTime.now())
                .build();

        // When
        eventPublisher.publishEvent(event);

        // Then
        List<UserAuditEvent> auditEvents = auditEventRepository.findByTargetUserId(102L);
        assertEquals(1, auditEvents.size());
        
        UserAuditEvent auditEvent = auditEvents.get(0);
        assertEquals(3L, auditEvent.getUserId());
        assertEquals(UserAuditEvent.AuditAction.USER_DELETED, auditEvent.getAction());
        assertEquals(102L, auditEvent.getTargetUserId());
        
        // Detailsの確認
        assertEquals("deleteduser", auditEvent.getDetails().get("username"));
        assertEquals("deleted@example.com", auditEvent.getDetails().get("email"));
        assertTrue(auditEvent.getDetails().containsKey("occurredAt"));
    }
}