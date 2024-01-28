package com.assessment.demo.controller;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/{requestUserId}/{receiveUserId}")
public class FriendController extends BaseController{
    /**
     * Friend controller, includes:<p>
     * + API add friend request 			(IN PROGRESS)<p>
     * + API friend response				(IN PROGRESS)
     */
    private final FriendService friendService;

    @Autowired
    public FriendController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository,
                            FriendService friendService) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
        this.friendService = friendService;
    }

    @PostMapping("/friend_request")
    public ResponseEntity<?> sendFriendRequest(@PathVariable("requestUserId") UUID requester,
                                               @PathVariable("receiveUserId") UUID receiver,
                                               HttpServletRequest request) {
        UsualResponse response = friendService.sendRequest(request);
        return responseEntity(response);
    }

    // Accept a friend request.
    @GetMapping("/friend_request/accept")
    public ResponseEntity<?> getAccessFriendRequest(@PathVariable("requestUserId") UUID requester,
                                                    @PathVariable("receiveUserId") UUID receiver,
                                                    HttpServletRequest request) {
        UsualResponse response = friendService.acceptFriendRequest(request);
        return responseEntity(response);
    }

    // Reject a friend request.
    @GetMapping("/friend_request/reject")
    public ResponseEntity<?> getRejectFriendRequest(@PathVariable("requestUserId") UUID requester,
                                                    @PathVariable("receiveUserId") UUID receiver,
                                                    HttpServletRequest request) {
        UsualResponse response = friendService.rejectFriendRequest(request);
        return responseEntity(response);
    }

    @DeleteMapping("/friend_remove")
    public ResponseEntity<?> removeFriend(@PathVariable("requestUserId") UUID requester,
                                                    @PathVariable("receiveUserId") UUID receiver,
                                                    HttpServletRequest request) {
        UsualResponse response = friendService.removeFriend(request);
        return responseEntity(response);
    }
}
