package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.dto.LoginRequest;
import carametal.practice.dto.UserRegistrationRequest;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import carametal.practice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private Role employeeRole;
    private Role adminRole;
    private User testAdminUser;

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

        // JWT認証テスト用の管理者ユーザーを作成
        testAdminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .registrationDate(LocalDateTime.now())
                .roles(Set.of(adminRole))
                .build();
        testAdminUser.setCreatedBy(1L);
        testAdminUser.setUpdatedBy(1L);
        userRepository.save(testAdminUser);
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

    private String getJwtToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("adminpass");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void JWT認証でユーザー登録が成功すること() throws Exception {
        String token = getJwtToken();
        
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
