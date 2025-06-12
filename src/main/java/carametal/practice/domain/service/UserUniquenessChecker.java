package carametal.practice.domain.service;

import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;

public interface UserUniquenessChecker {
    boolean isUsernameUnique(Username username);
    boolean isEmailUnique(Email email);
    boolean isUsernameUniqueExcluding(Username username, Long excludeUserId);
    boolean isEmailUniqueExcluding(Email email, Long excludeUserId);
}