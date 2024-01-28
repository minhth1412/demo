package com.assessment.demo.controller;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/user/{receiveUserId}")
public class FriendController extends BaseController {
    /**
     * Friend controller, includes:<p>
     * + API add friend request 			(IN PROGRESS)<p>
     * + API friend response				(IN PROGRESS)
     */
    private final FriendService friendService;

    @Autowired
    public FriendController(AuthService authService, JwtService jwtService, PostService postService, UserService userService, UserRepository userRepository, PostRepository postRepository,
                            FriendService friendService) {
        super(authService, jwtService, postService, userService, userRepository, postRepository);
        this.friendService = friendService;
    }

    @PostMapping("/friend_request")
    public ResponseEntity<?> sendFriendRequest(@PathVariable("receiveUserId") UUID receiver,
                                               HttpServletRequest request) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = friendService.sendRequest(user, receiver);
        return responseEntity(response);
    }

    // Accept a friend request.
    @GetMapping("/friend_request/accept")
    public ResponseEntity<?> getAccessFriendRequest(@PathVariable("receiveUserId") UUID receiver,
                                                    HttpServletRequest request) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = friendService.acceptFriendRequest(user, receiver);
        return responseEntity(response);
    }

    // Reject a friend request.
    @GetMapping("/friend_request/reject")
    public ResponseEntity<?> getRejectFriendRequest(@PathVariable("receiveUserId") UUID receiver,
                                                    HttpServletRequest request) {
        User user = checkUserSession(request);
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");

        UsualResponse response = friendService.rejectFriendRequest(user, receiver);
        return responseEntity(response);
    }

//    @DeleteMapping("/unfriend")
//    public ResponseEntity<?> removeFriend(@PathVariable("receiveUserId") UUID receiver,
//                                          HttpServletRequest request) {
//        User user = checkUserSession(request);
//        if (user ==null)
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token!");
//
//        UsualResponse response = friendService.removeFriend(user, receiver);
//        return responseEntity(response);
//    }
}
