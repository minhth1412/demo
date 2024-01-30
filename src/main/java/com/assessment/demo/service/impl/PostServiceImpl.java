package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.request.ReactRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.dto.response.PostDto;
import com.assessment.demo.entity.*;
import com.assessment.demo.repository.*;
import com.assessment.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReactRepository reactRepository;
    private final CommentRepository commentRepository;
    private final NotifyRepository notifyRepository;

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
            List<Post> Posts = postRepository.findByAuthor(user);
            return UsualResponse.success(user.getUsername(), PostDto.createPostsList(Posts, user));
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
                PostDto.createPostDto(post, user));
    }

    @Override
    public UsualResponse deletePostByPostId(User user,UUID postId) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");
        if (Objects.equals(user.getUsername(), post.getAuthor().getUsername())) {
            postRepository.deletePostByPostId(postId);
            return UsualResponse.success("Post deleted!");
        }
        return UsualResponse.error(HttpStatus.FORBIDDEN, "You do not have permission to do this!");
    }

    @Override
    public UsualResponse getAllPosts() {
        List<Post> Posts = postRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
        List<PostDto> allPostDTOs = PostDto.createPostsList(Posts);
        return UsualResponse.success("Welcome to news feed", allPostDTOs);
    }

    @Override
    public UsualResponse getPostByPostId(UUID postId) {

        Post post = postRepository.findByPostId(postId).orElse(null);

        return (post != null) ? UsualResponse.success("Post found",
                PostDto.builder().createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .author(post.getAuthor().getUsername())
                        .authorImage(post.getAuthor().getImage())
                        .status(post.getStatus())
                        .location(post.getLocation())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .reactCount(post.getReacts().size())
                        .commentCount(post.getComments().size())
                        //.sharedCount(post.getChildrenPosts.size())    , this will be deployed later
                        .build()) :
                UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");
    }

    // Future work: check if exist another react of current user with this post, change the reaction type:
    // - If they have the same reactType, change the status of previous reaction into false
    // - Else, change the previous reactType of that user to this post
    @Override
    public UsualResponse addReactionToAPost(User user, UUID postId, ReactRequest reactRequest) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");
        // Handle duplicate react here, now it is auto add up for each reqwuest
        //...
        React react = new React(reactRequest.getReactionType());
        post.addReact(react);
        Notify notify = new Notify(post.getAuthor(), "User " + user.getUsername() + " reacted on your post!");
        reactRepository.save(react);
        postRepository.save(post);
        notifyRepository.save(notify);
        return UsualResponse.success("Reacted to the post successfully!");
    }

    @Override
    public UsualResponse addComment(User user, UUID postId, Map<String, String> commentRequest) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");

        Comment comment = new Comment(post, null, commentRequest.get("content"), user);
        post.addComment(comment);
        Notify notify = new Notify(post.getAuthor(), "User " + user.getUsername() + " commented on your post!");

        commentRepository.save(comment);
        postRepository.save(post);
        notifyRepository.save(notify);
        return UsualResponse.success("Comment added to the post successfully!");
    }

    @Override
    public UsualResponse sharePost(User user, UUID postId, PostRequest request) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");

        Post rePost = post.sharePost(user);
        postRepository.save(rePost);
        return UsualResponse.success("You shared this post!",
                PostDto.createPostDto(rePost, user));
    }

    @Override
    public UsualResponse replyComment(User user, UUID postId, UUID commentId, Map<String, String> commentRequest) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");

        Comment parentCmt = commentRepository.findById(commentId).orElse(null);
        if (parentCmt == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Comment not found!");

        Comment comment = new Comment(post, parentCmt, commentRequest.get("content"), user);
        commentRepository.save(comment);
        return UsualResponse.success("Reply comment added to the post successfully!");
    }

}
