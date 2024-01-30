package com.assessment.demo.service;

import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    // Define needed variables
    void createAdminAccountIfNotExists(int roleId);

    public UserDetailsService userDetailsService();

    UsualResponse updateUser(UpdateUserInfoRequest infoRequest,User user);

    public List<User> findUsersByPartialUsername(String partialUsername, String roleName);

    int getTotalUsers(String query);

    UsualResponse getNotify(User user);
}
