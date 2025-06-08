package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.LoginRequest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql("/test-data.sql")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    @Test
    void registerUser_システム管理者権限_正常ケース() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("EMPLOYEE")));
    }

    @Test
    void registerUser_ユーザー管理者権限_正常ケース() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("SYSTEM_ADMIN"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("SYSTEM_ADMIN")));
    }

    @Test
    void registerUser_従業員権限_アクセス拒否() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        String token = getJwtToken("employee@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerUser_未認証_アクセス拒否() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerUser_バリデーションエラー_空のユーザー名() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_バリデーションエラー_無効なメールアドレス() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_重複エラー() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("testadmin@example.com")
                .password("password123")
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_システム管理者権限_正常ケース() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");
        
        // employeeユーザーのIDを取得
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(delete("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_存在しないユーザー() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(delete("/api/users/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_従業員権限_アクセス拒否() throws Exception {
        String token = getJwtToken("employee@example.com", "password123");
        
        // useradminユーザーのIDを取得
        Long useradminId = userRepository.findByUsername("useradmin").orElseThrow().getId();

        mockMvc.perform(delete("/api/users/" + useradminId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_未認証_アクセス拒否() throws Exception {
        // useradminユーザーのIDを取得
        Long useradminId = userRepository.findByUsername("useradmin").orElseThrow().getId();
        
        mockMvc.perform(delete("/api/users/" + useradminId))
                .andExpect(status().isForbidden());
    }

    private String getJwtToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

}
