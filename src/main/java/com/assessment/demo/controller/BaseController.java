package com.assessment.demo.controller;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
@RestController
@RequiredArgsConstructor
public abstract class BaseController {
    protected final AuthService authService;
    protected final JwtService jwtService;
    protected final PostService postService;
    protected final UserService userService;
    protected final UserRepository userRepository;
    protected final PostRepository postRepository;
    protected static ResponseEntity<?> responseEntity(UsualResponse response) {
        if (response.getData() == null)
            return ResponseEntity.status(response.getStatus()).body(response.getMessage());
        else
            return ResponseEntity.status(response.getStatus()).body(response.getData());
    }

      protected UsualResponse checkUserAuthentication(HttpServletRequest request,User user) {
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
}
