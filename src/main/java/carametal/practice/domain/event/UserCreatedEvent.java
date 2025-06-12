package carametal.practice.domain.event;

import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserCreatedEvent {
    private final Long userId;
    private final Long createdBy;
    private final Username username;
    private final Email email;
    private final Set<String> roleNames;
    private final LocalDateTime occurredAt;
}