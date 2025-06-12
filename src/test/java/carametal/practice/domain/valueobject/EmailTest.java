package carametal.practice.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void 有効なメールアドレスで作成成功() {
        Email email = new Email("test@example.com");
        assertEquals("test@example.com", email.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@domain.com",
        "user.name@domain.com",
        "user+tag@domain.co.jp",
        "user123@domain-name.org"
    })
    void 有効なメールアドレス形式(String validEmail) {
        Email email = new Email(validEmail);
        assertEquals(validEmail.toLowerCase(), email.getValue());
    }

    @Test
    void メールアドレスが空文字でエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Email(""));
    }

    @Test
    void メールアドレスがnullでエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    void メールアドレスが空白のみでエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Email("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "@domain.com",
        "user@",
        "user@@domain.com",
        "user@domain",
        "user.domain.com"
    })
    void 無効なメールアドレス形式でエラー(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    void メールアドレスが長すぎるとエラー() {
        String longEmail = "a".repeat(90) + "@domain.com";
        assertThrows(IllegalArgumentException.class, () -> new Email(longEmail));
    }

    @Test
    void メールアドレスは小文字に変換される() {
        Email email = new Email("Test@Example.COM");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void メールアドレスの前後の空白は除去される() {
        Email email = new Email("  test@example.com  ");
        assertEquals("test@example.com", email.getValue());
    }

    @Test
    void toString_値が返される() {
        Email email = new Email("test@example.com");
        assertEquals("test@example.com", email.toString());
    }

    @Test
    void equals_同じ値で等しい() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("TEST@EXAMPLE.COM");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void equals_異なる値で等しくない() {
        Email email1 = new Email("test1@example.com");
        Email email2 = new Email("test2@example.com");
        assertNotEquals(email1, email2);
    }
}