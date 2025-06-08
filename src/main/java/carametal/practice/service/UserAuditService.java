package carametal.practice.service;

import carametal.practice.entity.UserAuditEvent;
import carametal.practice.repository.UserAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAuditService {

    private final UserAuditEventRepository auditEventRepository;

    public void logUserCreated(Long userId, Long targetUserId, Map<String, Object> details) {
        logAuditEvent(userId, UserAuditEvent.AuditAction.USER_CREATED, targetUserId, details);
    }

    public void logUserUpdated(Long userId, Long targetUserId, Map<String, Object> details) {
        logAuditEvent(userId, UserAuditEvent.AuditAction.USER_UPDATED, targetUserId, details);
    }

    public void logUserDeleted(Long userId, Long targetUserId, Map<String, Object> details) {
        logAuditEvent(userId, UserAuditEvent.AuditAction.USER_DELETED, targetUserId, details);
    }

    public void logRoleAssigned(Long userId, Long targetUserId, Map<String, Object> details) {
        logAuditEvent(userId, UserAuditEvent.AuditAction.ROLE_ASSIGNED, targetUserId, details);
    }

    public void logRoleRemoved(Long userId, Long targetUserId, Map<String, Object> details) {
        logAuditEvent(userId, UserAuditEvent.AuditAction.ROLE_REMOVED, targetUserId, details);
    }

    private void logAuditEvent(Long userId, UserAuditEvent.AuditAction action, Long targetUserId, Map<String, Object> details) {
        UserAuditEvent auditEvent = new UserAuditEvent();
        auditEvent.setUserId(userId);
        auditEvent.setAction(action);
        auditEvent.setTargetUserId(targetUserId);
        auditEvent.setDetails(details);

        auditEventRepository.save(auditEvent);
    }
}