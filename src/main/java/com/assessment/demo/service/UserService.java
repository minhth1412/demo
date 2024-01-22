package com.assessment.demo.service;

import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.Role;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

public interface UserService {
    // Define needed variables
    void createAdminAccountIfNotExists(int roleId);
    public UserDetailsService userDetailsService();

    JwtResponse updateUser(UpdateUserInfoRequest infoRequest, User user);
}
