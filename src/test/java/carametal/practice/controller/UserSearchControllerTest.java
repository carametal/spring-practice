package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.entity.Role;
import carametal.practice.entity.User;
import carametal.practice.repository.RoleRepository;
import carametal.practice.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import carametal.practice.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class UserSearchControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final Faker faker = new Faker();
    private User systemAdminUser;
    private User userAdminUser;
    private User employeeUser;
    private Role systemAdminRole;
    private Role userAdminRole;
    private Role employeeRole;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        setupRoles();
        setupTestUsers();
        createTestUsersForSearch();
    }

    private void setupRoles() {
        systemAdminRole = createRole("SYSTEM_ADMIN", "システム管理者");
        userAdminRole = createRole("USER_ADMIN", "ユーザー管理者");
        employeeRole = createRole("EMPLOYEE", "従業員");
    }

    private Role createRole(String roleName, String description) {
        Role role = Role.builder()
                .roleName(roleName)
                .description(description)
                .build();
        role.setCreatedBy(1L);
        role.setUpdatedBy(1L);
        return roleRepository.save(role);
    }

    private void setupTestUsers() {
        systemAdminUser = createUser("testadmin", "testadmin@example.com", Set.of(systemAdminRole));
        userAdminUser = createUser("useradmin", "useradmin@example.com", Set.of(userAdminRole));
        employeeUser = createUser("employee", "employee@example.com", Set.of(employeeRole));
    }

    private void createTestUsersForSearch() {
        testUsers = new ArrayList<>();
        
        // 検索テスト用のユーザーを作成
        testUsers.add(createUser("john_doe", "john.doe@example.com", Set.of(employeeRole)));
        testUsers.add(createUser("jane_doe", "jane.doe@test.com", Set.of(employeeRole)));
        testUsers.add(createUser("alice_smith", "alice.smith@company.com", Set.of(employeeRole)));
        testUsers.add(createUser("bob_johnson", "bob.johnson@enterprise.org", Set.of(employeeRole)));
        testUsers.add(createUser("charlie_brown", "charlie@peanuts.com", Set.of(employeeRole)));
        testUsers.add(createUser("david_wilson", "david.wilson@example.com", Set.of(employeeRole)));
        testUsers.add(createUser("emma_davis", "emma.davis@test.org", Set.of(employeeRole)));
        testUsers.add(createUser("frank_miller", "frank.miller@company.net", Set.of(employeeRole)));
        testUsers.add(createUser("grace_lee", "grace.lee@example.org", Set.of(employeeRole)));
        testUsers.add(createUser("henry_taylor", "henry.taylor@business.com", Set.of(employeeRole)));
    }

    private User createUser(String username, String email, Set<Role> roles) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .registrationDate(LocalDateTime.now())
                .roles(roles)
                .build();
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        return userRepository.save(user);
    }

    @Test
    void searchUsers_WithUsernamePattern_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].username", containsInAnyOrder("john_doe", "jane_doe")));
    }

    @Test
    void searchUsers_WithUsernamePartialMatch_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("alice_smith"));
    }

    @Test
    void searchUsers_WithEmailDomainFilter_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("email", "example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.content[*].email", everyItem(containsString("example.com"))));
    }

    @Test
    void searchUsers_WithNoParameters_ReturnsAllUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10))) // デフォルトページサイズ
                .andExpect(jsonPath("$.totalElements").value(13)) // 基本ユーザー3 + テストユーザー10
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void searchUsers_WithEmployeeRole_ReturnsForbidden() throws Exception {
        String token = getJwtToken(employeeUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_ページネーション_正常ケース() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "5")
                .param("sort", "username")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(13))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.sort.sorted", is(true)));
    }

    @Test
    void searchUsers_検索条件付きページネーション() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "admin")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].username", containsInAnyOrder("testadmin", "useradmin")));
    }

    @Test
    void searchUsers_メール検索付きページネーション() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("email", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].email", everyItem(containsString("test"))));
    }

    @Test
    void searchUsers_複数条件検索() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "john")
                .param("email", "example.com")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("john_doe"))
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"));
    }

    @Test
    void searchUsers_ソート降順() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "5")
                .param("sort", "username")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.sort.sorted", is(true)))
                .andExpect(jsonPath("$.content[0].username").value("useradmin")) // Z→A順で最初
                .andExpect(jsonPath("$.content[4].username").value("henry_taylor")); // 5番目
    }

    @Test
    void searchUsers_デフォルトパラメータ() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements").value(13));
    }

    @Test
    void searchUsers_ユーザー管理者権限_正常ケース() throws Exception {
        String token = getJwtToken(userAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(13));
    }

    @Test
    void searchUsers_検索結果なし() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), "password123");

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
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