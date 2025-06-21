package carametal.practice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // ユーザー登録APIのバリデーションエラー
    USER_REGISTER_USERNAME_REQUIRED("USER_REGISTER_001", "ユーザー名は必須です"),
    USER_REGISTER_USERNAME_SIZE("USER_REGISTER_002", "ユーザー名は3文字以上50文字以下で入力してください"),
    USER_REGISTER_EMAIL_REQUIRED("USER_REGISTER_003", "メールアドレスは必須です"),
    USER_REGISTER_EMAIL_INVALID("USER_REGISTER_004", "有効なメールアドレスを入力してください"),
    USER_REGISTER_EMAIL_SIZE("USER_REGISTER_005", "メールアドレスは100文字以下で入力してください"),
    USER_REGISTER_PASSWORD_REQUIRED("USER_REGISTER_006", "パスワードは必須です"),
    USER_REGISTER_PASSWORD_SIZE("USER_REGISTER_007", "パスワードは8文字以上100文字以下で入力してください");
    
    private final String code;
    private final String message;
}