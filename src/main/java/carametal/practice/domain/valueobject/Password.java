package carametal.practice.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Password {
    private final String rawValue;
    
    public Password(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (rawValue.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.rawValue = rawValue;
    }
    
    // セキュリティのため、toString()はマスクされた値を返す
    @Override
    public String toString() {
        return "****";
    }
}