package carametal.practice.repository;

import carametal.practice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByRoleName(String roleName);
    
    Set<Role> findByRoleNameIn(Set<String> roleNames);
    
    boolean existsByRoleName(String roleName);
}