package com.assessment.demo.service;


import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;

import java.util.List;
import java.util.UUID;

public interface PostService {
    public List<Post> getAllPostsForCurrentUser(String username);

    public UsualResponse createNewPost(PostRequest request,User user);

    public void deletePostByPostId(User user,UUID postId);


}
