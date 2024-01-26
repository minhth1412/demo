package com.assessment.demo.controller;

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
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final PostService postService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

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
                    .map(userX -> new UserDto(userX.getUsername(),userX.getUserId()))
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
    @GetMapping("/{username}")
    public ResponseEntity<?> getHomepage(@PathVariable String username,HttpServletRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        log.info("username: {}",user != null ? user.getUsername():null);
        UsualResponse response = checkUserAuthentication(request,user);

        if (response == null) {
            assert user != null;
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
            response = UsualResponse.success(res);
        }
        return ResponseEntity.status(response.getStatus()).body(response.getMessage());
    }

    private UsualResponse checkUserAuthentication(HttpServletRequest request,User user) {
        log.info("go to here first");
        if (user == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "This path does not existed!");
        else if (!jwtService.isTokenValid(jwtService.extractJwtFromRequest(request),user))
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Invalid token!");
        else if (!Objects.equals(user.getUsername(),jwtService.userFromJwtInRequest(request)))
            return UsualResponse.error(HttpStatus.FORBIDDEN,"You are not allowed to do this!");
        else if (!user.getIsOnline())
            return UsualResponse.error(HttpStatus.FORBIDDEN,"You need to login first!");
        else if (!user.isAccountNonExpired()) {
            return UsualResponse.error(HttpStatus.FORBIDDEN, "Your account is being locked!");
        }
        return null;
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
            }
            else
                response = UsualResponse.error(HttpStatus.BAD_REQUEST,"Error occurs while update information");
        }
        log.info("API calling to update user information ends here!");
        return responseEntity(response);
    }

    @PostMapping("/{userId}/create_new_post")
    public ResponseEntity<?> createNewPost(@PathVariable UUID userId,HttpServletRequest request,@RequestBody PostRequest postRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        UsualResponse response = checkUserAuthentication(request,user);
        if (response == null) {
            postService.createNewPost(postRequest, user);
        }
        Post post = new Post(postRequest.getContent(),postRequest.getTitle(),
                postRequest.getImage(),postRequest.getLocation(),user);

        postRepository.save(post);
        return ResponseEntity.ok().body("Your post is published!");
    }
}
