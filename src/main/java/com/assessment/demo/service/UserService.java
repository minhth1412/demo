package com.assessment.demo.service;

import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    // Define needed variables
    @Value("${spring.account.admin.username}")
    String username;
    @Value("${spring.account.admin.password}")
    String password;
    @Value("${spring.account.admin.email}")
    String email;
    @Value("${spring.account.admin.firstname}")
    String firstname;
    @Value("${spring.account.admin.lastname}")
    String lastname;

    private final UserRepository userRepository;
    private final RoleService roleService;

    public void createAdminAccountIfNotExists(int roleId) {
        Role adminRole = roleService.getRoleById(roleId);
        if (adminRole == null) {
            throw new RuntimeException("Admin role with ID " + roleId + " not found.");
        }

        userRepository.findByRole(adminRole)
                .orElseGet(() -> createUser(username, password, email, firstname, lastname, adminRole));
    }

    private User createUser(String username, String password, String email, String firstName, String lastName, Role role) {
        log.info("Created a new user: " + username);
        return new User(username, new BCryptPasswordEncoder().encode(password), email, firstName, lastName, new Date(), role);
    }
}
