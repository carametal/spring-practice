package carametal.practice.infrastructure;

import carametal.practice.domain.event.UserCreatedEvent;
import carametal.practice.domain.event.UserDeletedEvent;
import carametal.practice.domain.event.UserUpdatedEvent;
import carametal.practice.service.UserAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listener for user domain events.
 * Handles the translation of domain events to audit log entries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserAuditService userAuditService;

    /**
     * Handles UserCreatedEvent and logs the user creation audit event.
     */
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.debug("Handling UserCreatedEvent for user ID: {}", event.getUserId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("username", event.getUsername().getValue());
        details.put("email", event.getEmail().getValue());
        details.put("roles", event.getRoleNames());
        details.put("occurredAt", event.getOccurredAt());
        
        userAuditService.logUserCreated(
            event.getCreatedBy(),
            event.getUserId(),
            details
        );
    }

    /**
     * Handles UserUpdatedEvent and logs the user update audit event.
     */
    @EventListener
    public void handleUserUpdatedEvent(UserUpdatedEvent event) {
        log.debug("Handling UserUpdatedEvent for user ID: {}", event.getUserId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("oldUsername", event.getOldUsername().getValue());
        details.put("newUsername", event.getNewUsername().getValue());
        details.put("oldEmail", event.getOldEmail().getValue());
        details.put("newEmail", event.getNewEmail().getValue());
        details.put("oldRoles", event.getOldRoleNames());
        details.put("newRoles", event.getNewRoleNames());
        details.put("occurredAt", event.getOccurredAt());
        
        userAuditService.logUserUpdated(
            event.getUpdatedBy(),
            event.getUserId(),
            details
        );
    }

    /**
     * Handles UserDeletedEvent and logs the user deletion audit event.
     */
    @EventListener
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        log.debug("Handling UserDeletedEvent for user ID: {}", event.getUserId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("username", event.getUsername().getValue());
        details.put("email", event.getEmail().getValue());
        details.put("occurredAt", event.getOccurredAt());
        
        userAuditService.logUserDeleted(
            event.getDeletedBy(),
            event.getUserId(),
            details
        );
    }
}