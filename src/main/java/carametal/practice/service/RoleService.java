package carametal.practice.service;

import carametal.practice.entity.Role;
import carametal.practice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    public Set<Role> findRolesByNames(Set<String> roleNames) {
        Set<Role> roles = roleRepository.findByRoleNameIn(roleNames);
        validateAllRolesExist(roleNames, roles);
        return roles;
    }
    
    private void validateAllRolesExist(Set<String> requestedNames, Set<Role> foundRoles) {
        Set<String> foundNames = foundRoles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        
        Set<String> missingRoles = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toSet());
                
        if (!missingRoles.isEmpty()) {
            throw new IllegalArgumentException("Invalid roles: " + missingRoles);
        }
    }
}