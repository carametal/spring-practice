package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import carametal.practice.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql("/test-data.sql")
class UserSearchControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void searchUsers_WithUsernameAndEmail_ReturnsMatchingUsers() throws Exception {
        User user1 = User.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user1.setCreatedBy(1L);
        user1.setUpdatedBy(1L);
        
        User user2 = User.builder()
            .username("jane_doe")
            .email("jane@test.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user2.setCreatedBy(1L);
        user2.setUpdatedBy(1L);

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/api/users/search")
                .param("username", "doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void searchUsers_WithUsernameOnly_ReturnsMatchingUsers() throws Exception {
        User user = User.builder()
            .username("john_smith")
            .email("john@smith.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);

        userRepository.save(user);

        mockMvc.perform(get("/api/users/search")
                .param("username", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("john_smith"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void searchUsers_WithEmailOnly_ReturnsMatchingUsers() throws Exception {
        User user = User.builder()
            .username("alice_wonder")
            .email("alice@wonderland.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);

        userRepository.save(user);

        mockMvc.perform(get("/api/users/search")
                .param("email", "wonderland"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("alice@wonderland.com"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void searchUsers_WithNoParameters_ReturnsAllUsers() throws Exception {
        User user1 = User.builder()
            .username("bob_builder")
            .email("bob@builder.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user1.setCreatedBy(1L);
        user1.setUpdatedBy(1L);
        
        User user2 = User.builder()
            .username("charlie_brown")
            .email("charlie@peanuts.com")
            .password("password")
            .registrationDate(LocalDateTime.now())
            .build();
        user2.setCreatedBy(1L);
        user2.setUpdatedBy(1L);

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchUsers_WithoutAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_ページネーション_正常ケース() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "2")
                .param("sort", "username")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", greaterThan(2)))
                .andExpect(jsonPath("$.totalPages", greaterThan(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.sort.sorted", is(true)));
    }

    @Test
    void searchUsers_検索条件付きページネーション() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "admin")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].username", containsString("admin")));
    }

    @Test
    void searchUsers_メール検索付きページネーション() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("email", "example.com")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].email", containsString("example.com")));
    }

    @Test
    void searchUsers_複数条件検索() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "test")
                .param("email", "example.com")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username", containsString("test")))
                .andExpect(jsonPath("$.content[0].email", containsString("example.com")));
    }

    @Test
    void searchUsers_ソート降順() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "email")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sort.sorted", is(true)));
    }

    @Test
    void searchUsers_デフォルトパラメータ() throws Exception {
        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void searchUsers_従業員権限_アクセス拒否() throws Exception {
        String token = getJwtToken("employee@example.com", "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_未認証_アクセス拒否_WithToken() throws Exception {
        mockMvc.perform(get("/api/users/search"))
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