package carametal.practice.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UsernameTest {

    @Test
    void 有効なユーザー名で作成成功() {
        Username username = new Username("validuser");
        assertEquals("validuser", username.getValue());
    }

    @Test
    void ユーザー名が空文字でエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Username(""));
    }

    @Test
    void ユーザー名がnullでエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Username(null));
    }

    @Test
    void ユーザー名が空白のみでエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Username("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a"})
    void ユーザー名が短すぎるとエラー(String shortName) {
        assertThrows(IllegalArgumentException.class, () -> new Username(shortName));
    }

    @Test
    void ユーザー名が長すぎるとエラー() {
        String longName = "a".repeat(51);
        assertThrows(IllegalArgumentException.class, () -> new Username(longName));
    }

    @Test
    void ユーザー名の前後の空白は除去される() {
        Username username = new Username("  validuser  ");
        assertEquals("validuser", username.getValue());
    }

    @Test
    void 境界値テスト_3文字() {
        Username username = new Username("abc");
        assertEquals("abc", username.getValue());
    }

    @Test
    void 境界値テスト_50文字() {
        String name50chars = "a".repeat(50);
        Username username = new Username(name50chars);
        assertEquals(name50chars, username.getValue());
    }

    @Test
    void toString_値が返される() {
        Username username = new Username("testuser");
        assertEquals("testuser", username.toString());
    }

    @Test
    void equals_同じ値で等しい() {
        Username username1 = new Username("testuser");
        Username username2 = new Username("testuser");
        assertEquals(username1, username2);
        assertEquals(username1.hashCode(), username2.hashCode());
    }

    @Test
    void equals_異なる値で等しくない() {
        Username username1 = new Username("testuser1");
        Username username2 = new Username("testuser2");
        assertNotEquals(username1, username2);
    }
}