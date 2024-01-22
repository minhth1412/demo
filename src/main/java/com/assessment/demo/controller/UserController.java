package com.assessment.demo.controller;

import com.assessment.demo.dto.UserDto;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user/")
public class UserController {
    private final PostService postService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping("search")
    public ResponseEntity<?> searchByUsername(@RequestParam String username) {
        try {
            List<User> searchResults = userRepository.searchUsersByUsername(username);
            // map User entities to a DTO if you only want to expose certain information
            // For simplicity, let's assume UserDTO is a DTO class representing a simplified User entity
            List<UserDto> userDTOs = searchResults.stream()
                    .map(user -> new UserDto(user.getUsername(),user.getUserId()))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(userDTOs,HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) ||
                !org.apache.commons.lang3.StringUtils.startsWith(authHeader,"Bearer ")) {
            String errorMessage = "The token is not in the Bearer token format!";
            log.info(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // Extract and return the token from the Authorization header
        return authHeader.substring(7);
    }

    private boolean isUserNotLoggingIn(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        String username = jwtService.extractUsername(jwt);
        return userRepository.findByUsername(username).isEmpty();
    }

    private String userFromToken(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return jwtService.extractUsername(jwt);
    }


    // Base on UserId on search api, we have that become input here
    // If the userId belongs to the current user, it returns all posts of that user.
    // Else, base on the relationship between the user with the owner userId, the posts will appear or not.
    // second problem will be deployed later.
    @GetMapping("profile/{userId}")
    public ResponseEntity<?> getHomepage(@PathVariable UUID userId,HttpServletRequest request) {
        // This hardcode will be fixed later for current user
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");

        if (isUserNotLoggingIn(request))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        List<Post> posts = postService.getAllPostsForCurrentUser(user.getUsername());
        // It returns ok, because current user don't have any posts yet
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }


    @PostMapping("setting/{userId}")
    public ResponseEntity<?> updateInfo(@PathVariable UUID userId,HttpServletRequest request,@RequestBody UpdateUserInfoRequest infoRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");
        if (isUserNotLoggingIn(request))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
        if (!Objects.equals(user.getUsername(),userFromToken(request))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to do this!");
        }
        JwtResponse response = userService.updateUser(infoRequest, user);
        log.info("Here comes the update");
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Update information successfully! Redirect to homepage...");

    }


//    @PostMapping("create_post/{userId}")
//    public ResponseEntity<?> createNewPost(@PathVariable UUID userId,HttpServletRequest request,@RequestBody PostRequest postRequest) {
//
//    }
}
