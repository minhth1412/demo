package com.assessment.demo.service;


import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.request.ReactRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PostService {
    public List<Post> getAllPostsForCurrentUser(String username);
    public UsualResponse getAllPostsForCurrentUser(UUID userId);

    public UsualResponse createNewPost(PostRequest request,User user);

    UsualResponse sharePost(User user, UUID postId, PostRequest request);

    public UsualResponse replyComment(User user, UUID postId, UUID commentId, Map<String, String> commentRequest);

    public UsualResponse deletePostByPostId(User user,UUID postId);

    public UsualResponse getAllPosts();

    public UsualResponse getPostByPostId(UUID postId);

    UsualResponse addReactionToAPost(User user, UUID postId, ReactRequest reactRequest);

    UsualResponse addComment(User user, UUID postId, Map<String, String> commentRequest);

//    UsualResponse sharePost(User user, UUID postId, PostRequest request);
}
