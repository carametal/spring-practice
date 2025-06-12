package carametal.practice.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Username {
    private final String value;
    
    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        this.value = value.trim();
    }
    
    @Override
    public String toString() {
        return value;
    }
}