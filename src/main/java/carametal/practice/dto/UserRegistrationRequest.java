package carametal.practice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "{USER_REGISTER_001}")
    @Size(min = 3, max = 50, message = "{USER_REGISTER_002}")
    private String username;

    @NotBlank(message = "{USER_REGISTER_003}")
    @Email(message = "{USER_REGISTER_004}")
    @Size(max = 100, message = "{USER_REGISTER_005}")
    private String email;

    @NotBlank(message = "{USER_REGISTER_006}")
    @Size(min = 8, max = 100, message = "{USER_REGISTER_007}")
    private String password;

    private Set<String> roleNames;
}
