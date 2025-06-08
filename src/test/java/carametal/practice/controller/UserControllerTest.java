package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.LoginRequest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
    }

    @Test
    @Sql("/test-data.sql")
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
    @Sql("/test-data.sql")
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
    @Sql("/test-data.sql")
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
    @Sql("/test-data.sql")
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
    @Sql("/test-data.sql")
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
    @Sql("/test-data.sql")
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

    @Test
    @Sql("/test-data.sql")
    void JWT認証でユーザー登録が成功すること() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");
        
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("jwtuser")
                .email("jwtuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jwtuser")))
                .andExpect(jsonPath("$.email", is("jwtuser@example.com")));

        assertThat(userRepository.findByEmail("jwtuser@example.com")).isPresent();
    }

    @Test
    @Sql("/test-data.sql")
    void JWT認証なしではユーザー登録が拒否されること() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("unauthuser")
                .email("unauthuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql("/test-data.sql")
    void 無効なJWTトークンではユーザー登録が拒否されること() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("invaliduser")
                .email("invaliduser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .header("Authorization", "Bearer invalid.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
