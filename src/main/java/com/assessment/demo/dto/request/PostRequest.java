package com.assessment.demo.dto.request;

import com.assessment.demo.entity.Comment;
import com.assessment.demo.entity.Enum.PostStatus;
import com.assessment.demo.entity.Interact;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    // private UUID postId; -> This is auto generated
    // private User author; -> This is generated from token

    private String title;       // Can be blank

    @NotBlank(message = "Post content cannot be blank")
    private String content;

    private PostStatus status;      // Set public, friend only or private. This default is public

    // This is used for checking if this post is repost or not
    private boolean isShared;
    private UUID postSharedId;
}
