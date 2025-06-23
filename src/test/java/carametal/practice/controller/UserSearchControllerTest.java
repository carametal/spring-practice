package carametal.practice.controller;

import carametal.practice.base.BaseIntegrationTest;
import carametal.practice.entity.User;
import carametal.practice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserSearchControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

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
                .andExpect(jsonPath("$.length()").value(2));
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
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("john_smith"));
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
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("alice@wonderland.com"));
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
                .andExpect(jsonPath("$.length()").value(2));
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
}