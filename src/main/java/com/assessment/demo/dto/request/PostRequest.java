package com.assessment.demo.dto.request;

import com.assessment.demo.entity.Enum.PostStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
public class PostRequest {
    // private UUID postId; -> This is auto generated
    // private User author; -> This is generated from token
    private String title;       // Can be blank
    @NotBlank(message = "Post content cannot be blank")
    private String content;
    private PostStatus status;      // Set public, friend only or private. This default is public
    // This is used for checking if this post is repost or not
    private String image;
    private boolean isShared;
    private UUID postSharedId;
    private String location;
}
