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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/post")
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
    @PostMapping("/{userId}/create_new_post")
    public ResponseEntity<?> createNewPost(@PathVariable UUID userId,HttpServletRequest request,@RequestBody PostRequest postRequest) {
        User user = userRepository.findByUserId(userId).orElse(null);
        UsualResponse response = checkUserAuthentication(request,user);

        if (response == null) {
            response = postService.createNewPost(postRequest, user);
        }
        else log.info(response.getMessage());
        return new ResponseEntity<>(response,response.getStatus());
    }

}
