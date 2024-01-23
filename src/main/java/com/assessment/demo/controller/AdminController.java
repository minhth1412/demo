package com.assessment.demo.controller;

import com.assessment.demo.dto.PostDto;
import com.assessment.demo.dto.request.UpdateUserInfoRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.dto.response.admin.UserDto;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/admin/")
public class AdminController {

    private final PostService postService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${spring.role_admin.name}")
    private String roleName;

    @Value("${spring.account.admin.username}")
    private String adminName;

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
    // it will throw error if 1 of 3 things above does not match
    private boolean isTokenValid(HttpServletRequest request,User user) {
        String jwt = extractJwtFromRequest(request);
        String username = jwtService.extractUsername(jwt);
        return userRepository.findByUsername(username).isEmpty() &&
                jwtService.isTokenValid(jwt,user);
    }

    private boolean isAdminSession(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        String username = jwtService.extractUsername(jwt);
        return Objects.equals(username,adminName);
    }

    @GetMapping("search/users")
    public ResponseEntity<?> searchAllUsers(HttpServletRequest request) {
        try {
            if (!isAdminSession(request)) {
                throw new RuntimeException("Invalid action!");
            }
            List<User> searchResults = userRepository.findAll();
//            List<UserDto> userDTOs = searchResults.stream()
//                    .map(user -> new UserDto(user.getUserId(),
//                            user.getUsername(),
//                            user.getRole().getRoleName(),
//                            user.getFirst_name(),
//                            user.getLast_name(),
//                            user.getEmail(),
//                            user.getPassword(),
//                            user.getStatus(),
//                            user.getIsDeleted(),
//                            user.getIsOnline(),
//                            user.getBio(),
//                            user.getImage(),
//                            user.getDateOfBirth()))
//                    .collect(Collectors.toList());



            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            String res = gson.toJson(userDTOs);
            log.info("Go to here and finish");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
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
//        if (!Objects.equals(user.getUsername(),userFromToken(request))) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to do this!");
//        }
        JwtResponse response = userService.updateUser(infoRequest,user);
        log.info("Here comes the update");
        if (response.getMsg() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Update information successfully! Redirect to homepage...");
    }

}