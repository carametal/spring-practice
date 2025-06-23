package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.LoginRequest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.dto.UserUpdateRequest;
import carametal.practice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .roleNames(Set.of("EMPLOYEE"))
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
                .roleNames(Set.of("EMPLOYEE"))
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
                .roleNames(Set.of("EMPLOYEE"))
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
                .roleNames(Set.of("EMPLOYEE"))
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
                .roleNames(Set.of("EMPLOYEE"))
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
                .roleNames(Set.of("EMPLOYEE"))
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

    @Test
    void updateUser_システム管理者権限_正常ケース() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_employee")
                .email("updated@example.com")
                .roleNames(Set.of("USER_ADMIN"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updated_employee")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("USER_ADMIN")));
    }

    @Test
    void updateUser_ユーザー管理者権限_正常ケース() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_employee2")
                .email("updated2@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("useradmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updated_employee2")))
                .andExpect(jsonPath("$.email", is("updated2@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("EMPLOYEE")));
    }

    @Test
    void updateUser_存在しないユーザー() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_user")
                .email("updated@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");

        mockMvc.perform(put("/api/users/999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_従業員権限_アクセス拒否() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_user")
                .email("updated@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("employee@example.com", "password123");
        Long useradminId = userRepository.findByUsername("useradmin").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + useradminId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_未認証_アクセス拒否() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_user")
                .email("updated@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        Long useradminId = userRepository.findByUsername("useradmin").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + useradminId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_バリデーションエラー_空のユーザー名() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("")
                .email("updated@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_バリデーションエラー_無効なメールアドレス() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_user")
                .email("invalid-email")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_重複エラー_ユーザー名() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("testadmin")
                .email("updated@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_重複エラー_メールアドレス() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("updated_employee")
                .email("testadmin@example.com")
                .roleNames(Set.of("EMPLOYEE"))
                .build();

        String token = getJwtToken("testadmin@example.com", "password123");
        Long employeeId = userRepository.findByUsername("employee").orElseThrow().getId();

        mockMvc.perform(put("/api/users/" + employeeId)
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
    void searchUsers_未認証_アクセス拒否() throws Exception {
        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isForbidden());
    }

}
