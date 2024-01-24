package com.assessment.demo.dto.response.post;

import com.assessment.demo.entity.Enum.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class PostDto {
    private String author;
    private String authorImage;
    private Date createdAt;

    private String title;
    private String content;     // This may include media, photo,...

    private int interactCount;
    private int commentCount;
    private int sharedCount;

    private PostStatus status;

    // nullable fields
    private Date updatedAt;
    private String location;    // if the post writer added it into the post
}
