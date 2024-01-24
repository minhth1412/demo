package com.assessment.demo.service;


import com.assessment.demo.entity.Post;
import java.util.List;

public interface PostService {
    public List<Post> getAllPostsForCurrentUser(String username);
}
