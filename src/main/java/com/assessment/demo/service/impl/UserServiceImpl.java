package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Notify;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Value("${spring.role_admin.name}")
    private String roleAdminName;

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final JwtService jwtService;

    public void createAdminAccountIfNotExists(int roleId) {
        Role adminRole = roleService.getRoleById(roleId);
        if (adminRole == null) {
            throw new RuntimeException("Admin role with ID " + roleId + " not found.");
        }
        var user = userRepository.findUserByRole(adminRole);
        if (user.isEmpty())
            userRepository.save(new User(username, new BCryptPasswordEncoder().encode(password),
                    email, firstname, lastname, adminRole, null, null, false));
    }

    @Override
    public UserDetailsService userDetailsService(){
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
    public List<User> findUsersByPartialUsername(String partialUsername, String roleName) {
        log.info("Searching for users with partial username: " + partialUsername);
        if (Objects.equals(roleName, roleAdminName)) {
            return userRepository.findByUsernameContaining(partialUsername);
        }
        return userRepository.findByRoleRoleNameAndUsernameContainingIgnoreCase(roleName, partialUsername);
    }


    @Override
    public int getTotalUsers(String query) {
        return 0;//~~
    }

    @Override
    public UsualResponse getNotify(User user) {
        Set<Notify> notifications = user.getNotifications();
        return UsualResponse.success("Notification of current user:", notifications);
    }

    @Override
    public UsualResponse updateUser(UpdateUserInfoRequest infoRequest, User user) {
        String msg;
        try {
            // The Email change can be separate with this later with the 3rd party authentication
            //  but now just using this change
            String oldUsername = user.getUsername();
            String newUsername = infoRequest.getUsername();
            user.updateInfo(newUsername, infoRequest.getFirstname(), infoRequest.getLastname(),
                    infoRequest.getEmail(), infoRequest.getBio(), infoRequest.getImage());
            userRepository.save(user);
            JwtResponse response = JwtResponse.fromUserWithoutToken(user);
            // The jwt is checked, so we need to update token with new username here if it is modified
            if (!Objects.equals(oldUsername,newUsername)) {
                String token = user.getToken().getCompressedTokenData();
                String refreshToken = user.getToken().getCompressedRefreshTokenData();
                jwtService.changeUsernameInToken(token, newUsername, user, true);
                jwtService.changeUsernameInToken(refreshToken, newUsername, user, false);
                response = JwtResponse.fromUserWithToken(user);
            }
            msg = "Update information successfully! Redirect to homepage...";
            log.info(msg);
            return UsualResponse.success(msg, response);
        } catch (Exception e) {
            msg = "There is error occurs in update user information: " + e.getMessage();
            log.error(msg);
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, msg);
        }
    }
}
