package com.assessment.demo.controller;

import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.UserDTOforAdmin;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.TokenRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin controller has author on these APIs:<p>
 * 1/ CRUD in USER table:<p>
 * -> Create a user account + setup role (/admin/create_new_user)<p>
 * -> Read the user table account (/admin/search/all)      (This is the getAll)<p>
 * -> Update a user status (/admin/update/user_status)		(Note: this can be set off...)<p>
 * -> Delete a user (/admin/delete?user=username}<p>
 * 2/ Get all (Read API above)<p>
 * 3/ Get one (Get by userId)<p>
 * 4/ Search user (By a name), return results that have username contain the name given.
 */
@RestController
@Slf4j
@RequestMapping("/api/admin")
public class AdminController extends BaseController {
    @Value("${spring.role_admin.name}")
    private String roleName;

    // Constructor
    @Autowired
    public AdminController(AuthService authService, JwtService jwtService, PostService postService, UserService userService, UserRepository userRepository, PostRepository postRepository, TokenRepository tokenRepository) {
        super(authService, jwtService, postService, userService, userRepository, postRepository, tokenRepository);
    }

    // Support method for checking administrator authority
    private String checkAdminSession(HttpServletRequest request) {
        String token = jwtService.extractJwtFromRequest(request);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElse(null);
        // Check if token is valid, include is expired or not.
        if (token == null)
            return "Invalid token";
        if (user == null)
            return "User is not existed!";
        if (!Objects.equals(user.getRole().getRoleName(), roleName))
            return "You do not have permission to enter this path!";
        if (!user.getIsOnline())
            return "Please log in first";
        return null;
    }

    // API create a new user
    @PostMapping("/create_new_user")
    public ResponseEntity<?> createNewUser(@RequestBody SignupRequest signupRequest, HttpServletRequest request) {
        String msg = checkAdminSession(request);
        if (msg != null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        return responseEntity(authService.signup(signupRequest));
    }

    // API to get a user profile
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getOneUser(@PathVariable UUID userId, HttpServletRequest request) {
        try {
            String res = checkAdminSession(request);
            if (res != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This action is forbidden!");
            }

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with userId: " + userId);
            }

            UserDTOforAdmin userDTO = new UserDTOforAdmin(user);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert userDTO to pretty-printed JSON
            res = gson.toJson(userDTO);
            log.info("This is the end of the get one user API calling.");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("An error occurred during the get one user.", e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the get one user.");
        }
    }


    // API search for all account profiles
    @GetMapping("/users")
    public ResponseEntity<?> searchAllUsers(HttpServletRequest request) {
        try {
            String res = checkAdminSession(request);
            if (res != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This action is forbidden!");
            }

            List<User> searchResults = userRepository.findAll();

            // Check if searchResults is null or empty before proceeding
            if (searchResults.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
            }

            List<UserDTOforAdmin> userDTOs = searchResults.stream()
                    .map(UserDTOforAdmin::new)
                    .collect(Collectors.toList());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert userDTOs list to pretty-printed JSON
            res = gson.toJson(userDTOs);
            log.info("This is the end of the search all user API calling.");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("An error occurred during the search.", e);

            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }


    // API for update user status: false -> user account is locked and vice versa.
    // User can not log in until it is opened back by admin
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserAccount(@PathVariable UUID userId,
                                               HttpServletRequest request) {
        String msg = checkAdminSession(request);
        if (msg != null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);

        User user = userRepository.findByUserId(userId)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
        } else if (user.getIsDeleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user account has already been deleted before!");
        }
        user.setIsDeleted(true);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("This user account is deleted!");
    }

    // API for searching user by username with query
    @GetMapping("/search")
    public ResponseEntity<?> searchUserByName(@RequestParam(name = "query", defaultValue = "") String partialUsername, HttpServletRequest request) {
        log.info("Enter searching user with partial name");
        try {
            String msg = checkAdminSession(request);
            if (msg != null)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
            User user = userRepository.findByUsername(jwtService.userFromJwtInRequest(request)).orElseThrow(() -> new RuntimeException("Wrong token!!!"));
            log.info("Enter searching user with partial name");
            List<User> searchResults = userService.findUsersByPartialUsername(partialUsername, user.getRole().getRoleName());
            if (searchResults.isEmpty()) {
                log.info("Can not find any username likes {}", partialUsername);
            }

            List<UserDTOforAdmin> userDTOs = searchResults.stream()
                    .map(UserDTOforAdmin::new)
                    .collect(Collectors.toList());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert userDTOs list to pretty-printed JSON
            String res = gson.toJson(userDTOs);
            log.info("This is the end of the search all user API calling.");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            log.error("An error occurred during the search.", e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }
}