package carametal.practice.repository;

import carametal.practice.entity.UserAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuditEventRepository extends JpaRepository<UserAuditEvent, Long> {
    List<UserAuditEvent> findByTargetUserId(Long targetUserId);
}