package com.assessment.demo.controller;

import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.post.PostDto;
import com.assessment.demo.dto.response.admin.UserDto;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.exception.UserNotExistException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin controller has author on these APIs:<p>
 * 1/ CRUD in USER table:<p>
 * -> Create a user account (/admin/create_new_user)<p>
 * -> Read the user table account (/admin/search/all)      (This is the getAll)<p>
 * -> Update a user status (/admin/update/user_status)		(Note: this can be set off...)<p>
 * -> Delete a user (/admin/delete?user=username}<p>
 * 2/ Get all (Read API above)<p>
 * 3/ Get one (Get by userId)<p>
 * 4/ Search user (By a name), return results that have username contain the name given.
 */
@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController extends BaseController {
    @Value("${spring.role_admin.name}")
    private String roleName;

    @Value("${spring.account.admin.username}")
    private String currentUsername;

    // Constructor
    public AdminController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    // Support method for checking administrator authority
    private boolean isNotAdminSession(HttpServletRequest request) {
        String username = jwtService.userFromJwtInRequest(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotExistException("User is not existed!"));
        String token = jwtService.extractJwtFromRequest(request);
        // If the user extracted from request's jwt = admin username;
        // And also, the token of that user = token get from the request jwt.
        // And also, the admin is online.
        return Objects.equals(username,currentUsername) &&
                Objects.equals(user.getToken().getCompressedTokenData(),token) &&
                user.getIsOnline();
    }

    // Methods handle APIs:
    // API create a new user
    @GetMapping("/create_new_user")
    public ResponseEntity<?> createNewUser(@PathVariable SignupRequest signupRequest,HttpServletRequest request) {
        if (isNotAdminSession(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid action!");
        }
        return responseEntity(authService.signup(signupRequest));
    }

    // API search for all account
    @GetMapping("/search/all")
    public ResponseEntity<?> searchAllUsers(HttpServletRequest request) {
        try {
            if (isNotAdminSession(request)) {
                log.info("This action is forbidden!");
                throw new RuntimeException("Forbidden action!");
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
            log.info("this is the end of the search all user api calling");
            return ResponseEntity.status(HttpStatus.OK).body(res);

        } catch (UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    // API for update user status: false -> user account is locked and vice versa.
    // User can not log in until it is opened back by admin
    @GetMapping("/updateStatus")
    public ResponseEntity<?> updateUserStatus(@RequestParam(name = "user") String username,
                                              @RequestParam(name = "status") boolean status,
                                              HttpServletRequest request) {
        if (isNotAdminSession(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid action!");
        }
        log.info("comes here after admin session validation");

        User user = userRepository.findByUsername(username).orElseThrow(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't exist");
        } else if (user.getStatus() == status) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The status is already set up with " + status);
        } else {
            user.setStatus(status);
            userRepository.save(user);
        }
        String res = (status) ? "can be used" : "is locked and can not be used!";
        return ResponseEntity.status(HttpStatus.OK).body("The status of user "
                + username + " is updated! Now this user account " + res);
    }

    // API delete user: change isDeleted from user, and the rest is normal
    // User can not log in until it is opened back by admin
    @GetMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam(name = "user") String username,
                                        HttpServletRequest request) {
        if (isNotAdminSession(request)) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid action!");
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't exist");
        } else if (user.getIsDeleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user is already deleted before!");
        } else {
            user.setIsDeleted(true);
            userRepository.save(user);
        }
        return ResponseEntity.status(HttpStatus.OK).body("User " + username + " is deleted!");
    }


    @GetMapping("/{userId}")
    public ResponseEntity<?> getHomepage(@PathVariable UUID userId,HttpServletRequest request) {
        if (isNotAdminSession(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid action!");
        }
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Input userId is not existed!");

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
    public ResponseEntity<?> searchUserByName(@RequestParam(name = "user") String partialUsername,HttpServletRequest request) {
        log.info("Enter searching user with partial name");
        try {
            if (isNotAdminSession(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden action");
            }
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }
}