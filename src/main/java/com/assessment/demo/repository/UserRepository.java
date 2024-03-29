package com.assessment.demo.repository;

import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByUsername(String username);

    List<User> findByRoleRoleNameAndUsernameContainingIgnoreCase(String roleName, String partialUsername);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Optional<User> findUserByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserId(UUID userId);

    @Query(value = "SELECT * FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', ?1, '%')) ESCAPE ''", nativeQuery = true)
    List<User> findByUsernameContaining(String partialUsername);

//    @Query("SELECT u FROM User u WHERE u.email = :email")
//    Optional<User> findUserByEmail(String email);
//    // If needed extension of fixed code, write those functions down here!
//    // ...
}
