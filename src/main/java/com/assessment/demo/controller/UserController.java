package com.assessment.demo.controller;

import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.post.PostDto;
import com.assessment.demo.dto.response.user.UserDto;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    public UserController(AuthService authService, JwtService jwtService, PostService postService, UserService userService, UserRepository userRepository, PostRepository postRepository) {
        super(authService, jwtService, postService, userService, userRepository, postRepository);
    }

    // API for update user information
    @PostMapping("/setting")
    public ResponseEntity<?> updateInfo(HttpServletRequest request, @RequestBody UpdateUserInfoRequest infoRequest) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        JwtResponse responseData = userService.updateUser(infoRequest, user);
        if (responseData != null) {
            responseData.setMsg("Update information successfully! Redirect to homepage...");
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        }
        return responseEntity(UsualResponse.error(HttpStatus.BAD_REQUEST, "Error occurs while update information"));
    }

    // API search for users with query name, if the query is empty, it returns all users
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam(name = "query", defaultValue = "") String query, HttpServletRequest request) {
        try {
            User user = checkUserSession(request);
            if (user == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

            List<User> searchResults = userService.findUsersByPartialUsername(query);
            // map User entities to a DTO with public information
            List<UserDto> userDTOs = UserDto.createUsersList(searchResults);

            // Return the response as JSON
            return ResponseEntity.ok(userDTOs);
            //return new ResponseEntity<>(userDTOs,HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    // API to reset user password
    @PutMapping("/change_password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = authService.resetPassword(resetPasswordRequest, request);
        return responseEntity(response);
    }
}
