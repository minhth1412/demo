package com.assessment.demo.controller;

import com.assessment.demo.dto.response.post.PostDto;
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
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {
    /**
     * Admin controller has author on these APIs:<p>
     * + /admin/search/all: return all the account that existed in db.<p>
     * + /admin/search?user={username}: return all the account that have username like the given<p>
     * + /admin/updateStatus?user={user}&status={statusValue}
     */

    private final PostService postService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${spring.role_admin.name}")
    private String roleName;

    @Value("${spring.account.admin.username}")
    private String currentUsername;

    /**
     * Check if token is valid, include:<p>
     * + user extract from the jwt exists<p>
     * + is token expires<p>
     * + is the user extract from the jwt equals with the user get from userId</p>
     * It will throw error if 1 of 3 things above does not match
     */
    private boolean isTokenValid(HttpServletRequest request,User user) {
        return userRepository.findByUsername(jwtService.extractJwtFromRequest(request)).isPresent() &&
                jwtService.isTokenValid(jwtService.extractJwtFromRequest(request),user);
    }

    private boolean isNotAdminSession(HttpServletRequest request) {
        return !Objects.equals(jwtService.extractJwtFromRequest(request),currentUsername);
    }

    // API search for all account
    @GetMapping("/search/all")
    public ResponseEntity<?> searchAllUsers(HttpServletRequest request) {
        try {
            if (isNotAdminSession(request)) {
                throw new RuntimeException("Invalid action!");
            }
            List<User> searchResults = userRepository.findAll();
            List<UserDto> userDTOs = searchResults.stream()
                    .map(user -> new UserDto(user.getUserId(),
                            user.getUsername(),
                            user.getRole().getRoleName(),
                            user.getFirst_name(),
                            user.getLast_name(),
                            user.getEmail(),
                            user.getPassword(),
                            user.getStatus(),
                            user.getIsDeleted(),
                            user.getIsOnline(),
                            user.getBio(),
                            user.getImage()
//                            ,user.getDateOfBirth()
                    ))
                    .collect(Collectors.toList());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            String res = gson.toJson(userDTOs);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    @GetMapping("/updateStatus")
    public ResponseEntity<?> updateUserStatus(@RequestParam(name = "user") String username,
                                              @RequestParam(name = "status") boolean status,
                                              HttpServletRequest request) {
        try {
            if (isNotAdminSession(request)) {
                throw new RuntimeException("Invalid action!");
            }
            User user = userRepository.findByUsername(username).orElseThrow(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't exist");
            } else if (user.getStatus() == status) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The status is already set up with " + status);
            }
            user.setStatus(status);
            userRepository.save(user);
            String res = (status) ? "can be used" : "is locked and can not be used!";
            return ResponseEntity.status(HttpStatus.OK).body("The status of user "
                    + username + " is updated! Now this user account " + res);
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
    @GetMapping("/{username}")
    public ResponseEntity<?> getHomepage(@PathVariable String username,HttpServletRequest request) {
        // This hardcode will be fixed later for current user
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This path does not existed!");

        if (isTokenValid(request,user))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!?");
        if (!user.getIsOnline()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Need to login first");
        }
        List<Post> Posts = postService.getAllPostsForCurrentUser(user.getUsername());
        // Convert Post entities to PostDto objects
        List<PostDto> postDtos = Posts.stream()
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

    @GetMapping("/search")
    public ResponseEntity<?> searchUserByName(@RequestParam(name = "user") String partialUsername) {
        log.info("Enter searching user with partial name");
        try {
            log.info("Enter searching user with partial name");
            List<User> searchResults = userService.findUsersByPartialUsername(partialUsername);
            if (searchResults.isEmpty()) {
                log.info("The result is empty and it is different with the change in sql");
            }
            List<UserDto> userDTOs = searchResults.stream()
                    .map(user -> new UserDto(user.getUserId(),
                            user.getUsername(),
                            user.getRole().getRoleName(),
                            user.getFirst_name(),
                            user.getLast_name(),
                            user.getEmail(),
                            user.getPassword(),
                            user.getStatus(),
                            user.getIsDeleted(),
                            user.getIsOnline(),
                            user.getBio(),
                            user.getImage()
//                            ,user.getDateOfBirth()
                    ))
                    .collect(Collectors.toList());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            String res = gson.toJson(userDTOs);
            log.info("Done searching by partial username: {}",partialUsername);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }

    }
}