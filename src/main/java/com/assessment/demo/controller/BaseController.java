package com.assessment.demo.controller;

import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        return new ResponseEntity<>(response, response.getStatus());
    }
//
//    protected UsualResponse checkUserAuthentication(HttpServletRequest request, User user) {
//        if (user == null)
//            return UsualResponse.error(HttpStatus.BAD_REQUEST, "This path does not existed!");
//        else if (!jwtService.isTokenValid(jwtService.extractJwtFromRequest(request), user))
//            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Invalid token!");
//        else if (!Objects.equals(user.getUsername(), jwtService.userFromJwtInRequest(request)))
//            return UsualResponse.error(HttpStatus.FORBIDDEN, "You are not allowed to do this!");
//        else if (!user.getIsOnline())
//            return UsualResponse.error(HttpStatus.FORBIDDEN, "You need to login first!");
//        else if (!user.isAccountNonExpired()) {
//            return UsualResponse.error(HttpStatus.FORBIDDEN, "Your account is being locked!");
//        }
//        return null;
//    }

    @Value("${spring.role_user.name}")
    private String role2_name;

    @Autowired
    protected final TokenRepository tokenRepository;
    // Method in this controller to check if the token is valid or not by jwt in request header
    protected User checkUserSession(HttpServletRequest request) {

        String jwt = jwtService.extractJwtFromRequest(request);
        if (jwt != null) {
            String currentUser = jwtService.extractUsername(jwt);
            User user = userRepository.findByUsername(currentUser).orElse(null);
            if (user != null) {
                String role = user.getRole().getRoleName();
                if (Objects.equals(role,role2_name)) {
                    Token userToken = tokenRepository.findByTokenId(user.getToken().getTokenId()).orElse(null);
                    if (jwt.equals(userToken != null ? userToken.getTokenData() : null)) {
                        if (user.getIsOnline()) {
                            return user;
                        }
                    }
                }
            }
        }
        return null;
    }

}
