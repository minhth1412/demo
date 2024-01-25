package com.assessment.demo.service;


import com.assessment.demo.dto.request.PostRequest;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;

import java.util.List;

public interface PostService {
    public List<Post> getAllPostsForCurrentUser(String username);

    public void createNewPost(PostRequest request,User user);


}
