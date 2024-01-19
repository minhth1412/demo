package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.RoleService;
import com.assessment.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
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
        userRepository.findUserByRole(adminRole)
                .orElseGet(() -> new User(username, new BCryptPasswordEncoder().encode(password), email, firstname, lastname, adminRole));
    }

    @Override
    public UserDetailsService userDetailsService(){
        return username -> userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
}