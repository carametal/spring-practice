package carametal.practice.repository;

import carametal.practice.entity.UserAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuditEventRepository extends JpaRepository<UserAuditEvent, Long> {
}