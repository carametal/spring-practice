package carametal.practice.util;

import carametal.practice.base.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
    "jwt.secret=myTestSecretKey123456789012345678901234567890123456789012345678901234567890",
    "jwt.expiration=86400000"
})
class JwtUtilTest extends BaseIntegrationTest {

    @Autowired
    private JwtUtil jwtUtil;
    
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    void トークンを生成できること() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void トークンからユーザー名を抽出できること() {
        String token = jwtUtil.generateToken(userDetails);
        
        String username = jwtUtil.extractUsername(token);
        
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void 有効なトークンを検証できること() {
        String token = jwtUtil.generateToken(userDetails);
        
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void 無効なトークンを検証できること() {
        String invalidToken = "invalid.token.here";
        
        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken, userDetails))
            .isInstanceOf(Exception.class);
    }

    @Test
    void トークンの有効期限を取得できること() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void 異なるユーザーのトークンは無効であること() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User("differentuser", "password", new ArrayList<>());
        
        boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertThat(isValid).isFalse();
    }
}