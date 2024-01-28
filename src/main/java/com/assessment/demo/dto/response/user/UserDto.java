package com.assessment.demo.dto.response.user;

import com.assessment.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private String image;

    public static List<UserDto> createUsersList(List<User> Users) {
        return Users.stream()
                .map(user -> new UserDto(user.getUsername()
                        ,user.getImage()))
                .collect(Collectors.toList());
    }
}
