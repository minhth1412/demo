package com.assessment.demo.controller;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api")
public class PostController extends BaseController{

    public PostController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    /**
     * Base on UserId on search api, we have that become input here<p>
     * If the userId belongs to the current user, it returns all posts of that user.<p>
     * Else, base on the relationship between the user with the owner userId, the posts will appear or not.<p>
     * second problem will be deployed later.<p>
     */
    @PostMapping("/post/create_new_post")
    public ResponseEntity<?> createNewPost(@PathVariable UUID userId,HttpServletRequest request,@RequestBody PostRequest postRequest) {
        // The userId in the URL needs to match with the userid that gets from the jwt in header request

        // User gets by userId in URL
        User user = userRepository.findByUserId(userId).orElse(null);

        // Call check method between request and URL
        UsualResponse response = checkUserAuthentication(request,user);

        // call create post method when the response is null, that means there was no error occurs
        if (response == null) {
            response = postService.createNewPost(postRequest, user);
        }
        else log.info(response.getMessage());

        return new ResponseEntity<>(response,response.getStatus());
    }

    // API for return a user posts
    @GetMapping("/{userId}/posts")
    public ResponseEntity<?> getAllPostsByUser(@PathVariable UUID userId){

        UsualResponse response = postService.getAllPostsForCurrentUser(userId);
        //return ResponseEntity.ok(postsByUserList);
        return new ResponseEntity<>(response, response.getStatus());
    }

    // APi for get news feed
    @GetMapping("/home")
    public ResponseEntity<?> getNewsFeed(){

        UsualResponse response = postService.getAllPosts();
        //return ResponseEntity.ok(postsByUserList);
        return new ResponseEntity<>(response, response.getStatus());
    }

    // API get one post base on postId, userId here belongs to the requested user.
    //  check that if the user can see the post or not.
    @GetMapping("user/{userId}/posts/{postId}")
    public  ResponseEntity<?> getPostByPostId(@PathVariable UUID userId, @PathVariable UUID postId){
        UsualResponse response = postService.getPostByPostId(userId, postId);
        return new ResponseEntity<>(response, response.getStatus());

    }
}
