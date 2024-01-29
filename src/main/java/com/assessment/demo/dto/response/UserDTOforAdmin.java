package com.assessment.demo.dto.response;

import com.assessment.demo.entity.User;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTOforAdmin {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean status;
    private Boolean isDeleted;
    private Boolean isOnline;
    private String bio;
    private String image;
    private String roleName;

    // Other fields can be added based on your requirements

    public UserDTOforAdmin(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.firstName = user.getFirst_name();
        this.lastName = user.getLast_name();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.isDeleted = user.getIsDeleted();
        this.isOnline = user.getIsOnline();
        this.bio = user.getBio();
        this.image = user.getImage();
        this.roleName = user.getRole().getRoleName();

        // Map other fields as needed later
    }
}