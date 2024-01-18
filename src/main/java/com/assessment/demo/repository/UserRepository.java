package com.assessment.demo.repository;

import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
//    @Query("SELECT u FROM User u WHERE u.username = :username")
//    Optional<User> findUserByUsername(String username);
//
//    Boolean existsByUsername(String username);
//
//    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Optional<User> findByRole(Role role);

//    @Query("SELECT u FROM User u WHERE u.email = :email")
//    Optional<User> findUserByEmail(String email);
//    // If needed extension of fixed code, write those functions down here!
//    // ...
}
