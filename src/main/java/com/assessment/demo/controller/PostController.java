package com.assessment.demo.controller;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.request.ReactRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.dto.response.PostDto;
import com.assessment.demo.entity.Post;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class PostController extends BaseController{

    @Autowired
    public PostController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository, TokenRepository tokenRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository, tokenRepository);
    }

    @PostMapping("/post/create_new_post")
    public ResponseEntity<?> createNewPost(HttpServletRequest request,@RequestBody PostRequest postRequest) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        // Call check method between request and URL
        UsualResponse response = postService.createNewPost(postRequest, user);
        log.info(response.getMessage());
        return new ResponseEntity<>(response,response.getStatus());
    }

    // Get a user profile, using their userId
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileWithUserId(@PathVariable UUID userId, HttpServletRequest request) {
        if (checkUserSession(request) == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("UserId not exists");

        List<Post> userPosts = postService.getAllPostsForCurrentUser(user.getUsername());
        List<PostDto> postDTOs = PostDto.createPostsList(userPosts, user);

        // Create a Gson instance with pretty-printing enabled
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Convert PostDto list to pretty-printed JSON
        String res = gson.toJson(postDTOs);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    // Retrieve the current user's homepage
    @GetMapping("/profile/myProfile")
    public ResponseEntity<?> getCurrentUserProfile(HttpServletRequest request) {
        try {
            User user = checkUserSession(request);
            if (user == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

            List<Post> userPosts = postService.getAllPostsForCurrentUser(user.getUsername());
            List<PostDto> postDTOs = PostDto.createPostsList(userPosts, user);

            // Create a Gson instance with pretty-printing enabled
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Convert PostDto list to pretty-printed JSON
            String res = gson.toJson(postDTOs);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the user's homepage.");
        }
    }


    // API get one post base on postId, userId here belongs to the requested user.
    //  check that if the user can see the post or not.
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getPostByPostId(@PathVariable UUID postId, HttpServletRequest request){
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = postService.getPostByPostId(postId);
        return responseEntity(response);

    }

    @PostMapping("/post/{postId}/react")
    public ResponseEntity<?> reactWithAPost(@PathVariable UUID postId, @RequestBody ReactRequest reactRequest, HttpServletRequest request){
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = postService.addReactionToAPost(user, postId, reactRequest);
        return responseEntity(response);
    }

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<?> commentAPost(@PathVariable UUID postId, @RequestBody Map<String, String> commentRequest, HttpServletRequest request){
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = postService.addComment(user, postId, commentRequest);
        return responseEntity(response);
    }

//    @PostMapping("/post/{postId}/share")
//    public ResponseEntity<?> shareAPost(@PathVariable UUID postId, @RequestBody PostRequest postRequest, HttpServletRequest request){
//        User user = checkUserSession(request);
//        if (user == null)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
//
//        UsualResponse response = postService.sharePost(user, postId, postRequest);
//        return new ResponseEntity<>(response, response.getStatus());
//    }

    @GetMapping(value = {"/post", "/home"})
    public ResponseEntity<?> getPostInNewsFeed(HttpServletRequest request){
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
        UsualResponse response = postService.getAllPosts();
        return responseEntity(response);
    }

}
