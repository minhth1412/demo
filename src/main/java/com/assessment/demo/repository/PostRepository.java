package com.assessment.demo.repository;

import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByAuthor(User author);

    Optional<Post> findByPostId(UUID postId);

    void deletePostByPostId(UUID postId);
}
