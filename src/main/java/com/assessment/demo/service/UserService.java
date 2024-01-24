package com.assessment.demo.service;

import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    // Define needed variables
    void createAdminAccountIfNotExists(int roleId);

    public UserDetailsService userDetailsService();

    JwtResponse updateUser(UpdateUserInfoRequest infoRequest,User user);

    public List<User> findUsersByPartialUsername(String partialUsername);

    List<User> searchUsers(String query,int page,int pageSize,String sort,String order);

    int getTotalUsers(String query);
}
