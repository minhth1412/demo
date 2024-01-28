package com.assessment.demo.controller;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.post.PostDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class PostController extends BaseController{

    @Autowired
    public PostController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    /**
     * Base on UserId on search api, we have that become input here<p>
     * If the userId belongs to the current user, it returns all posts of that user.<p>
     * Else, base on the relationship between the user with the owner userId, the posts will appear or not.<p>
     * second problem will be deployed later.<p>
     */
    @PostMapping("/create_new_post")
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
    @GetMapping("/{userId}")
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
    @GetMapping("/myProfile")
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
    @GetMapping("user/{userId}/posts/{postId}")
    public  ResponseEntity<?> getPostByPostId(@PathVariable UUID userId, @PathVariable UUID postId){
        UsualResponse response = postService.getPostByPostId(userId, postId);
        return new ResponseEntity<>(response, response.getStatus());

    }
}
