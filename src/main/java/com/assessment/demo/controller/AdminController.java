package com.assessment.demo.controller;

import com.assessment.demo.dto.request.SignupRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
@RequestMapping("/api/admin")
public class AdminController extends BaseController {
    @Value("${spring.role_admin.name}")
    private String roleName;

    // Constructor
    @Autowired
    public AdminController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
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
    @GetMapping("/create_new_user")
    public ResponseEntity<?> createNewUser(@PathVariable SignupRequest signupRequest,HttpServletRequest request) {
        String msg = checkAdminSession(request);
        if (msg != null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        return responseEntity(authService.signup(signupRequest));
    }

    // API to get a user profile
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getHomepage(@PathVariable UUID userId,HttpServletRequest request) {
        try {
            if (checkAdminSession(request) != null)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This action is forbidden!");

            User user = userRepository.findByUserId(userId).orElse(null);
            if (user == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UserId not exists!");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            log.info("this is the end of the search all user api calling");
            return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }

    // API search for all account profiles
    @GetMapping("/users")
    public ResponseEntity<?> searchAllUsers(HttpServletRequest request) {
        try {
            String res = checkAdminSession(request);
            if (res != null)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This action is forbidden!");

            List<User> searchResults = userRepository.findAll();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            res = gson.toJson(searchResults);
            log.info("this is the end of the search all user api calling");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User doesn't exist");
        } else if (!user.getStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user account has already been deleted before!");
        }
        user.setStatus(false);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("This user account is deleted!");
    }

    // API for searching user by username with query
    @GetMapping("/search")
    public ResponseEntity<?> searchUserByName(@RequestParam(name = "query", defaultValue = "") String partialUsername,HttpServletRequest request) {
        log.info("Enter searching user with partial name");
        try {
            String msg = checkAdminSession(request);
            if (msg != null)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);

            log.info("Enter searching user with partial name");
            List<User> searchResults = userService.findUsersByPartialUsername(partialUsername);
            if (searchResults.isEmpty()) {
                log.info("Can not find any username likes {}", partialUsername);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert searchResults list to pretty-printed JSON
            String res = gson.toJson(searchResults);
            log.info("Done searching by partial username: {}",partialUsername);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the search.");
        }
    }
}