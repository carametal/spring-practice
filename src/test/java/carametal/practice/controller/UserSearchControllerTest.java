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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final Faker japaneseFaker = new Faker(Locale.JAPAN);
    private User systemAdminUser;
    private User userAdminUser;
    private User employeeUser;
    private Role systemAdminRole;
    private Role userAdminRole;
    private Role employeeRole;
    private List<User> testUsers;
    
    // 検索テスト用の予測可能なデータ
    private String commonDomain;
    private String searchableLastName;
    private String testPassword;
    private String commonPrefix;

    @BeforeEach
    void setUp() {
        initializeTestData();
        setupRoles();
        setupTestUsers();
        createTestUsersForSearch();
    }
    
    private void initializeTestData() {
        // 検索テスト用の予測可能なデータを初期化
        commonDomain = faker.internet().domainName();
        searchableLastName = faker.name().lastName().toLowerCase();
        testPassword = faker.internet().password(8, 16, true, true, true);
        commonPrefix = faker.name().firstName().toLowerCase();
    }

    private void setupRoles() {
        systemAdminRole = createRole("SYSTEM_ADMIN", japaneseFaker.job().title());
        userAdminRole = createRole("USER_ADMIN", japaneseFaker.job().title());
        employeeRole = createRole("EMPLOYEE", japaneseFaker.job().title());
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
        systemAdminUser = createUser(
            faker.name().username() + "_admin", 
            faker.internet().emailAddress(), 
            Set.of(systemAdminRole)
        );
        userAdminUser = createUser(
            faker.name().username() + "_user", 
            faker.internet().emailAddress(), 
            Set.of(userAdminRole)
        );
        employeeUser = createUser(
            faker.name().username() + "_emp", 
            faker.internet().emailAddress(), 
            Set.of(employeeRole)
        );
    }

    private void createTestUsersForSearch() {
        testUsers = new ArrayList<>();
        
        // 検索テスト用のユーザーをFakerで生成
        // 共通のドメインを持つユーザー (メール検索用)
        testUsers.add(createUser(
            faker.name().firstName().toLowerCase() + "_" + searchableLastName,
            faker.name().firstName().toLowerCase() + "@" + commonDomain,
            Set.of(employeeRole)
        ));
        testUsers.add(createUser(
            faker.name().firstName().toLowerCase() + "_" + searchableLastName,
            faker.name().firstName().toLowerCase() + "@" + commonDomain,
            Set.of(employeeRole)
        ));
        
        // 共通の接頭辞を持つユーザー (ユーザー名検索用)
        testUsers.add(createUser(
            commonPrefix + "_" + faker.name().lastName().toLowerCase(),
            faker.internet().emailAddress(),
            Set.of(employeeRole)
        ));
        testUsers.add(createUser(
            commonPrefix + "_" + faker.name().lastName().toLowerCase(),
            faker.internet().emailAddress(),
            Set.of(employeeRole)
        ));
        
        // テスト用のドメインを持つユーザー
        String testDomain = "test.com";
        testUsers.add(createUser(
            faker.name().username(),
            faker.name().firstName().toLowerCase() + "@" + testDomain,
            Set.of(employeeRole)
        ));
        testUsers.add(createUser(
            faker.name().username(),
            faker.name().firstName().toLowerCase() + "@" + testDomain,
            Set.of(employeeRole)
        ));
        
        // その他のランダムユーザー
        for (int i = 0; i < 4; i++) {
            testUsers.add(createUser(
                faker.name().username(),
                faker.internet().emailAddress(),
                Set.of(employeeRole)
            ));
        }
    }

    private User createUser(String username, String email, Set<Role> roles) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(testPassword))
                .registrationDate(LocalDateTime.ofInstant(faker.timeAndDate().past(), ZoneId.systemDefault()))
                .roles(roles)
                .build();
        user.setCreatedBy(faker.number().numberBetween(1L, 10L));
        user.setUpdatedBy(faker.number().numberBetween(1L, 10L));
        return userRepository.save(user);
    }

    @Test
    void searchUsers_WithUsernamePattern_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", searchableLastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].username", everyItem(containsString(searchableLastName))));
    }

    @Test
    void searchUsers_WithUsernamePartialMatch_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", commonPrefix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].username", everyItem(containsString(commonPrefix))));
    }

    @Test
    void searchUsers_WithEmailDomainFilter_ReturnsMatchingUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("email", commonDomain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[*].email", everyItem(containsString(commonDomain))));
    }

    @Test
    void searchUsers_WithNoParameters_ReturnsAllUsers() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10))) // デフォルトページサイズ
                .andExpect(jsonPath("$.totalElements").value(13)) // 基本ユーザー3 + テストユーザー10
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void searchUsers_WithEmployeeRole_ReturnsForbidden() throws Exception {
        String token = getJwtToken(employeeUser.getEmail(), testPassword);

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
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

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
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", commonPrefix)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].username", everyItem(containsString(commonPrefix))));
    }

    @Test
    void searchUsers_メール検索付きページネーション() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

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
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", searchableLastName)
                .param("email", commonDomain)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].username", everyItem(containsString(searchableLastName))))
                .andExpect(jsonPath("$.content[*].email", everyItem(containsString(commonDomain))));
    }

    @Test
    void searchUsers_ソート降順() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "5")
                .param("sort", "username")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.sort.sorted", is(true)));
    }

    @Test
    void searchUsers_デフォルトパラメータ() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements").value(13));
    }

    @Test
    void searchUsers_ユーザー管理者権限_正常ケース() throws Exception {
        String token = getJwtToken(userAdminUser.getEmail(), testPassword);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(13));
    }

    @Test
    void searchUsers_検索結果なし() throws Exception {
        String token = getJwtToken(systemAdminUser.getEmail(), testPassword);
        String nonExistentKeyword = faker.lorem().word() + "_" + faker.number().digits(10);

        mockMvc.perform(get("/api/users/search")
                .header("Authorization", "Bearer " + token)
                .param("username", nonExistentKeyword))
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