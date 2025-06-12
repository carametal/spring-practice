package carametal.practice.infrastructure;

import carametal.practice.domain.service.UserUniquenessChecker;
import carametal.practice.domain.valueobject.Email;
import carametal.practice.domain.valueobject.Username;
import carametal.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUniquenessCheckerImpl implements UserUniquenessChecker {
    
    private final UserRepository userRepository;
    
    @Override
    public boolean isUsernameUnique(Username username) {
        return !userRepository.existsByUsername(username.getValue());
    }
    
    @Override
    public boolean isEmailUnique(Email email) {
        return !userRepository.existsByEmail(email.getValue());
    }
    
    @Override
    public boolean isUsernameUniqueExcluding(Username username, Long excludeUserId) {
        return userRepository.findByUsername(username.getValue())
                .map(user -> user.getId().equals(excludeUserId))
                .orElse(true);
    }
    
    @Override
    public boolean isEmailUniqueExcluding(Email email, Long excludeUserId) {
        return userRepository.findByEmail(email.getValue())
                .map(user -> user.getId().equals(excludeUserId))
                .orElse(true);
    }
}