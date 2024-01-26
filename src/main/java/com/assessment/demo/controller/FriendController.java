package com.assessment.demo.controller;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/friend-requests")
public class FriendController extends BaseController{

    public FriendController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    @PostMapping("")
    public ResponseEntity<?> sendFriendRequest(HttpServletRequest request) {
        return responseEntity(UsualResponse.success("This is undeveloped"));
    }

    //Get incoming friend requests.
    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingFriendRequest(HttpServletRequest request) {
        return responseEntity(UsualResponse.success("This is undeveloped"));
    }

    // Get outgoing friend requests.
    @GetMapping("outgoing")
    public ResponseEntity<?> getOutcomingFriendRequest(HttpServletRequest request) {
        return responseEntity(UsualResponse.success("This is undeveloped"));
    }

    // Accept a friend request.
    @PutMapping("/{id}/accept)")
    public ResponseEntity<?> acceptFriendRequest(HttpServletRequest request) {
        return responseEntity(UsualResponse.success("This is undeveloped"));
    }

    // Reject a friend request.
    @PutMapping("/{id}/reject)")
    public ResponseEntity<?> rejectFriendRequest(HttpServletRequest request) {
        return responseEntity(UsualResponse.success("This is undeveloped"));
    }

}
