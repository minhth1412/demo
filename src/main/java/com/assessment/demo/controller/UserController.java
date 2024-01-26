package com.assessment.demo.controller;

import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
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
import com.assessment.demo.service.AuthService;
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

import static com.assessment.demo.controller.BaseController.responseEntity;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController extends BaseController {
    /**
     * UD user's information:<p>
     * -> Update basic information of user      (/user/setting/{userId})<p>
     * -> reset password                    (/user/reset_password)<p>
     * + Api search user (by name), return less information of each user		(IN PROGRESS)<p>
     * + get one user homepage (or getUserById), if there are posts that are not being shared with user... (This is the difference from the Admin version, which return all posts)<p>
     * + api getCurrentUserProfile.			(Variation of get one above, with current userId)<p>
     * + API add friend request 			(IN PROGRESS)<p>
     * + API friend response				(IN PROGRESS)
     */

    public UserController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    @PostMapping("/setting/{userId}")
    public ResponseEntity<?> updateInfo(@PathVariable UUID userId,HttpServletRequest request,@RequestBody UpdateUserInfoRequest infoRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        UsualResponse response = checkUserAuthentication(request,user);
        if (response == null) {
            JwtResponse responseData = userService.updateUser(infoRequest,user);
            if (responseData != null) {
                responseData.setMsg("Update information successfully! Redirect to homepage...");
                response = UsualResponse.success(responseData);
            } else
                response = UsualResponse.error(HttpStatus.BAD_REQUEST,"Error occurs while update information");
        }
        log.info("API calling to update user information ends here!");
        return responseEntity(response);
    }

    // API search for users with input: a name that is contained in the usernames
    @GetMapping("/search")       // Looks like this: /user/search?query=...&page=1&pageSize=10&sort=username&order=asc
    public ResponseEntity<?> searchUsers(@RequestParam(name = "query") String query,HttpServletRequest request) {
        try {
            String jwt = jwtService.extractJwtFromRequest(request);
            String currentUser = jwtService.extractUsername(jwt);
            User user = userRepository.findByUsername(currentUser).orElse(null);

            if (user == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID TOKEN!");

            List<User> searchResults = userService.findUsersByPartialUsername(query);
            // map User entities to a DTO if you only want to expose certain information
            // For simplicity, let's assume UserDTO is a DTO class representing a simplified User entity
            List<UserDto> userDTOs = searchResults.stream()
                    .map(userX -> new UserDto(userX.getUsername(),userX.getUserId(), userX.getImage()))
                    .toList();
            List<User> users = userService.searchUsers(query);

            // Calculate total pages based on the total users and pageSize

            // Create a UserSearchResponse DTO
            UserSearchResponse response = new UserSearchResponse(users);

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
    @GetMapping("/{userId}")
    public ResponseEntity<?> getHomepageWithUserId(@PathVariable UUID userId,HttpServletRequest request) {
        User user = userRepository.findByUserId(userId).orElse(null);
        UsualResponse response = checkUserAuthentication(request,user);
        if (response != null)
            return responseEntity(response);

        assert user != null;
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
}
