package carametal.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    
    private Long id;
    private String username;
    private String email;
    private LocalDateTime registrationDate;
    private Set<String> roleNames;
}