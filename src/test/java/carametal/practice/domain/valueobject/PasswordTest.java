package carametal.practice.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    void 有効なパスワードで作成成功() {
        Password password = new Password("password123");
        assertEquals("password123", password.getRawValue());
    }

    @Test
    void パスワードが空文字でエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Password(""));
    }

    @Test
    void パスワードがnullでエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Password(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "abc", "a", ""})
    void パスワードが短すぎるとエラー(String shortPassword) {
        if (!shortPassword.isEmpty()) {
            assertThrows(IllegalArgumentException.class, () -> new Password(shortPassword));
        }
    }

    @Test
    void 境界値テスト_6文字() {
        Password password = new Password("123456");
        assertEquals("123456", password.getRawValue());
    }

    @Test
    void toString_マスクされた値が返される() {
        Password password = new Password("supersecret");
        assertEquals("****", password.toString());
    }

    @Test
    void equals_同じ値で等しい() {
        Password password1 = new Password("password123");
        Password password2 = new Password("password123");
        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }

    @Test
    void equals_異なる値で等しくない() {
        Password password1 = new Password("password123");
        Password password2 = new Password("password456");
        assertNotEquals(password1, password2);
    }
}