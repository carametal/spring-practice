package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

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

    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        Role empRole = Role.builder()
                .roleName("EMPLOYEE")
                .description("従業員")
                .build();
        empRole.setCreatedBy(1L);
        empRole.setUpdatedBy(1L);
        employeeRole = roleRepository.save(empRole);

        Role sysAdminRole = Role.builder()
                .roleName("SYSTEM_ADMIN")
                .description("システム管理者")
                .build();
        sysAdminRole.setCreatedBy(1L);
        sysAdminRole.setUpdatedBy(1L);
        adminRole = roleRepository.save(sysAdminRole);
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void registerUser_システム管理者権限_正常ケース() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("EMPLOYEE")));
    }

    @Test
    @WithMockUser(roles = "USER_ADMIN")
    void registerUser_ユーザー管理者権限_正常ケース() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roleNames(Set.of("SYSTEM_ADMIN"))
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roleNames[0]", is("SYSTEM_ADMIN")));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void registerUser_従業員権限_アクセス拒否() throws Exception {
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
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void registerUser_バリデーションエラー_空のユーザー名() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void registerUser_バリデーションエラー_無効なメールアドレス() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void registerUser_重複エラー() throws Exception {
        User existingUser = User.builder()
                .username("existing")
                .email("test@example.com")
                .password("password")
                .build();
        existingUser.setCreatedBy(1L);
        existingUser.setUpdatedBy(1L);
        userRepository.save(existingUser);

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
