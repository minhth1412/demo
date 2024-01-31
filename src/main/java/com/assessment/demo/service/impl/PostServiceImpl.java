package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.request.StringRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.dto.response.PostDto;
import com.assessment.demo.entity.*;
import com.assessment.demo.entity.Enum.TypeReact;
import com.assessment.demo.repository.*;
import com.assessment.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UsualResponse addReactionToAPost(User user, UUID postId, StringRequest stringRequest) {
        log.info("1");
        Optional<Post> optionalPost = postRepository.findByPostId(postId);
        // If post is not found in database, return error
        if (optionalPost.isEmpty()) {
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Post not found!");
        }
        log.info("2");
        Post post = optionalPost.get();
        TypeReact newTypeReact;

        // If the input name did not match with any type in typeReact, return error
        try {
            newTypeReact = TypeReact.valueOf(stringRequest.getString().toUpperCase());
            log.info("3");
        } catch (IllegalArgumentException e) {
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Invalid reaction type!");
        }
        // Check if user has already reacted the post:
        Optional<React> existingReact = findUserReactForPost(user, post);
        log.info("4");
        // If result found
        if (existingReact.isPresent()) {
            React react = existingReact.get();
            log.info("6");
            if (react.getTypeReact() == newTypeReact) {
                // User is reacting with the same type again, set status to false
                log.info("7");
                react.updateStatus(!react.getStatus());
            } else {
                // User is reacting with a different type, update the typeReact
                log.info("8");
                react.updateReaction(newTypeReact);
            }
            saveEntities(post, react);
            log.info("9");
            return UsualResponse.success("React updated successfully!");
        } else {
            // User hasn't reacted before, create a new reaction
            log.info("10");
            React newReact = new React(UUID.randomUUID(), newTypeReact, user);
            log.info("12");
            saveEntities(post, newReact);
            log.info("13");
            return UsualResponse.success("Reacted to the post successfully!");
        }
    }

    // Find the user's existing reaction for the post
    private Optional<React> findUserReactForPost(User user, Post post) {
        try {
            return post.getReacts().stream()
                    .filter(existingReact -> existingReact.getSender().equals(user))
                    .findFirst();
        } catch(NullPointerException e){
            return Optional.empty();
        }
    }

    // Method to create new notification for the other user, and save the objects into database through the repositories
    private void saveEntities(Post post, React react) {
        Notify notify = new Notify(post.getAuthor(), "User " + react.getSender().getUsername() + " reacted on your post!");
        log.info("12");
        reactRepository.save(react);
        log.info("13");
        postRepository.save(post);
        log.info("14");
        notifyRepository.save(notify);
        log.info("15");
    }





// ...

    @Override
    public UsualResponse addComment(User user, UUID postId, StringRequest commentRequest) {
        Optional<Post> optionalPost = postRepository.findByPostId(postId);

        optionalPost.ifPresent(post -> {
            Comment comment = new Comment(post, null, commentRequest.getString(), user);
            post.addComment(comment);
            Notify notify = new Notify(post.getAuthor(), "User " + user.getUsername() + " commented on your post!");

            commentRepository.save(comment);
            notifyRepository.save(notify);
        });

        return optionalPost.map(post -> UsualResponse.success("Comment added to the post successfully!"))
                .orElseGet(() -> UsualResponse.error(HttpStatus.NOT_FOUND, "Post not found!"));
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
