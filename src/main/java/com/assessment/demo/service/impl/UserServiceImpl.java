package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.RoleService;
import com.assessment.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    /**
     * The variables that contain the admin account setting up.
     * <p>
     * Additionally, it is marked with {@link Value} to load the values defined in
     * application.yml. Missing this file will cause undefined error.
     */
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
    private final JwtService jwtService;
    private final AuthService authService;

    public void createAdminAccountIfNotExists(int roleId) {
        Role adminRole = roleService.getRoleById(roleId);
        if (adminRole == null) {
            throw new RuntimeException("Admin role with ID " + roleId + " not found.");
        }
        var user = userRepository.findUserByRole(adminRole);
        if (user.isEmpty())
            userRepository.save(new User(username, new BCryptPasswordEncoder().encode(password),
                    email, firstname, lastname, adminRole, null, null, null, false));
    }

    @Override
    public UserDetailsService userDetailsService(){
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
    public List<User> findUsersByPartialUsername(String partialUsername) {
        System.out.println("Searching for users with partial username: " + partialUsername);
        List<User> users = userRepository.findByUsernameContaining(partialUsername);
        System.out.println("Found " + users.size() + " users.");
//        List<User> users1 = userRepository.findByUsername(Username);
//        System.out.println("Found " + users1.size() + " users.");
        return users;
        //return userRepository.findByUsernameContaining(partialUsername);
    }

    @Override
    public List<User> searchUsers(String query) {
        return null;//~~
    }

    @Override
    public int getTotalUsers(String query) {
        return 0;//~~
    }

    @Override
    public JwtResponse updateUser(UpdateUserInfoRequest infoRequest, User user) {
        try {
            // The Email change can be separate with this later with the 3rd party authentication
            //  but now just using this change
            String oldUsername = user.getUsername();
            user.updateInfo(infoRequest.getUsername(), infoRequest.getFirstname(), infoRequest.getLastname(),
                    infoRequest.getEmail(), infoRequest.getBio(), infoRequest.getImage(), infoRequest.getDateOfBirth());
            userRepository.save(user);
            // The jwt is checked, so we need to update token with new username here if it is modified
            if (!Objects.equals(oldUsername,user.getUsername())) {
                jwtService.refreshToken(user, false);
            }
            String msg = "Update information successfully!";
            log.info(msg);
            return JwtResponse.fromUserWithToken(user, msg);
        } catch (Exception e) {
            log.error("There is error occurs in update user information: " + e.getMessage());
            return null;
        }
    }
}
