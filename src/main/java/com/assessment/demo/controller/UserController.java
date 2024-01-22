package com.assessment.demo.controller;

import com.assessment.demo.dto.UserDto;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user/")
public class UserController {
    private final PostService postService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("profile/{username}")
    public ResponseEntity<?> getAllPosts(@PathVariable String username) {
        // This hardcode will be fixed later for current user
        var user = userService.userDetailsService().loadUserByUsername(username);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path is not existed!");
        List<Post> posts = postService.getAllPostsForCurrentUser(username);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("search")
    public ResponseEntity<?> searchByUsername(@RequestParam String username) {
        try {
            List<User> searchResults = userRepository.searchUsersByUsername(username);
            // map User entities to a DTO if you only want to expose certain information
            // For simplicity, let's assume UserDTO is a DTO class representing a simplified User entity
            List<UserDto> userDTOs = searchResults.stream()
                    .map(user -> new UserDto(user.getUsername(), user.getUserId()))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(userDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    // Base on UserId on search api, we have that become input here
    @GetMapping("profile/{userId}")
    public ResponseEntity<?> getOtherHomepage(@PathVariable UUID userId) {
        // This hardcode will be fixed later for current user
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path is not existed!");
        List<Post> posts = postService.getAllPostsForCurrentUser(user.getUsername());
        return new ResponseEntity<>(posts,HttpStatus.OK);
    }
}
