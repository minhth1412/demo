package com.assessment.demo.controller;

import com.assessment.demo.dto.response.post.PostDto;
import com.assessment.demo.dto.response.user.UserDto;
import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.dto.response.user.UserSearchResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    private final PostRepository postRepository;

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

    // Check if token is valid, include:
    //  + user extract from the jwt exists
    //  + is token expires
    //  + is the user extract from the jwt equals with the user get from userId
    private boolean isTokenValid(HttpServletRequest request,User user) {
        String jwt = extractJwtFromRequest(request);
        String username = jwtService.extractUsername(jwt);

        return userRepository.findByUsername(username).isEmpty() &&
                jwtService.isTokenValid(jwt,user);
    }

    private String userFromToken(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return jwtService.extractUsername(jwt);
    }

    @GetMapping("search")       // Looks like this: /user/search?query=...&page=1&pageSize=10&sort=username&order=asc
    public ResponseEntity<?> searchUsers(@RequestParam(name = "query") String query,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                                         @RequestParam(name = "sort", defaultValue = "username") String sort,
                                         @RequestParam(name = "order", defaultValue = "asc") String order,
                                         HttpServletRequest request) {
        try {
            String jwt = extractJwtFromRequest(request);
            String currentUser = jwtService.extractUsername(jwt);
            User user = userRepository.findByUsername(currentUser).orElse(null);

            if (user == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID TOKEN!");

            List<User> searchResults = userService.findUsersByPartialUsername(query);
            // map User entities to a DTO if you only want to expose certain information
            // For simplicity, let's assume UserDTO is a DTO class representing a simplified User entity
            List<UserDto> userDTOs = searchResults.stream()
                    .map(userX -> new UserDto(userX.getUsername(),userX.getUserId()))
                    .collect(Collectors.toList());
            List<User> users = userService.searchUsers(query, page, pageSize, sort, order);

            // Calculate total pages based on the total users and pageSize
            int totalUsers = userService.getTotalUsers(query);
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

            // Create a UserSearchResponse DTO
            UserSearchResponse response = new UserSearchResponse(users, page, pageSize, totalPages, totalUsers);

            // Return the response as JSON
            return ResponseEntity.ok(response);
            //return new ResponseEntity<>(userDTOs,HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }


    // Base on UserId on search api, we have that become input here
    // If the userId belongs to the current user, it returns all posts of that user.
    // Else, base on the relationship between the user with the owner userId, the posts will appear or not.
    // second problem will be deployed later.
    @GetMapping("{username}")
    public ResponseEntity<?> getHomepage(@PathVariable String username,HttpServletRequest request) {
        // This hardcode will be fixed later for current user
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");

        if (isTokenValid(request,user) && !user.getIsOnline())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        List<Post> posts = postService.getAllPostsForCurrentUser(user.getUsername());
        // Convert Post entities to PostDto objects
        List<PostDto> postDtos = posts.stream()
                .map(post -> PostDto.builder()
                        .author(user.getUsername())
                        .authorImage(user.getImage())
                        .createdAt(post.getCreatedAt())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .interactCount(post.getInteracts().size())
                        .commentCount(post.getComments().size())
                        //.sharedCount(post.getSharedCount())
                        .status(post.getStatus())
                        .updatedAt(post.getUpdatedAt())
                        .location(post.getLocation())
                        .build())
                .collect(Collectors.toList());

        // Create a Gson instance with pretty-printing enabled
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert PostDto list to pretty-printed JSON
        String res = gson.toJson(postDtos);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }


    @PostMapping("setting/{userId}")
    public ResponseEntity<?> updateInfo(@PathVariable UUID userId,HttpServletRequest request,@RequestBody UpdateUserInfoRequest infoRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");
        if (isTokenValid(request,user) && !user.getIsOnline())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
        if (!Objects.equals(user.getUsername(),userFromToken(request))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to do this!");
        }
        JwtResponse response = userService.updateUser(infoRequest,user);
        log.info("Here comes the update");
        if (response.getMsg() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Update information successfully! Redirect to homepage...");
    }

    @PostMapping("{userId}/create_new_post")
    public ResponseEntity<?> createNewPost(@PathVariable UUID userId,HttpServletRequest request,@RequestBody PostRequest postRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");
        if (isTokenValid(request,user) && !user.getIsOnline())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
        if (!Objects.equals(user.getUsername(),userFromToken(request))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to do this!");
        }

        Post post = new Post(postRequest.getContent(),postRequest.getTitle(),
                postRequest.getImage(),postRequest.getLocation(),user);

        postRepository.save(post);
        return ResponseEntity.ok().body("Your post is published!");
    }
}
