package com.assessment.demo.repository;

import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByUsername(String username);

    List<User> findByRoleRoleNameNot(String roleName);

    Optional<Token> findTokenByUsername(String token);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Optional<User> findUserByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> searchUsersByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserId(UUID userId);

//    @Query("SELECT u FROM User u WHERE u.email = :email")
//    Optional<User> findUserByEmail(String email);
//    // If needed extension of fixed code, write those functions down here!
//    // ...
}
