package com.assessment.demo.dto.response.user;

import com.assessment.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserSearchResponse {

    private List<User> users;
    private int page;
    private int pageSize;
    private int totalPages;
    private int totalUsers;
    // Getters and setters
}

