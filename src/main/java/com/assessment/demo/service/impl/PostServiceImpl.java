package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.post.PostDto;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public List<Post> getAllPostsForCurrentUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return postRepository.findByAuthor(user);
        } else {
            // Handle case where the user is not found
            return Collections.emptyList();
        }
    }

    @Override
    public UsualResponse getAllPostsForCurrentUser(UUID userId) {
        User user = userRepository.findByUserId(userId).orElse(null);

        if (user != null) {
            return UsualResponse.success(user.getUsername(), postRepository.findByAuthor(user));
        } else {
            // Handle case where the user is not found
            return UsualResponse.success("This user does not have any posts!");
        }
    }

    @Override
    public UsualResponse createNewPost(PostRequest request,User user) {
        Post post = new Post(request.getContent(),request.getTitle(),
                request.getImage(),request.getLocation(),user);
        postRepository.save(post);
        return UsualResponse.success("Your post is published",
                PostDto.builder().createdAt(post.getCreatedAt())
                        .title(post.getTitle())
                        .interactCount(0)
                        .commentCount(0)
                        .sharedCount(0)
                        .author(post.getAuthor().getUsername())
                        .authorImage(post.getAuthor().getImage())
                        .updatedAt(post.getUpdatedAt())
                        .status(post.getStatus())
                        .location(post.getLocation())
                        .content(post.getContent()).build());
    }

    @Override
    public void deletePostByPostId(User user,UUID postId) {
        postRepository.deletePostByPostId(postId);
    }

    @Override
    public UsualResponse getAllPosts() {
        return UsualResponse.success("Welcome to news feed",
                postRepository.findAll(Sort.by(Sort.Order.desc("createdAt"))));
    }

    @Override
    public UsualResponse getPostByPostId(UUID userId, UUID postId) {

        Post post = postRepository.findByPostId(postId).orElse(null);
        return (post != null) ? UsualResponse.success("Post found!", post) :
                UsualResponse.success("Post not found!");
    }

}
