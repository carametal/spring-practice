package carametal.practice.domain.event;

import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserDeletedEvent {
    private final Long userId;
    private final Long deletedBy;
    private final Username username;
    private final Email email;
    private final LocalDateTime occurredAt;
}