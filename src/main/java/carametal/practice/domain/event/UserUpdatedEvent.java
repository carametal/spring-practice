package carametal.practice.domain.event;

import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserUpdatedEvent {
    private final Long userId;
    private final Long updatedBy;
    private final Username oldUsername;
    private final Username newUsername;
    private final Email oldEmail;
    private final Email newEmail;
    private final Set<String> oldRoleNames;
    private final Set<String> newRoleNames;
    private final LocalDateTime occurredAt;
}