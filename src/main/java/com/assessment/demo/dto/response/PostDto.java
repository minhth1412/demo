package com.assessment.demo.dto.response;

import com.assessment.demo.entity.Enum.PostStatus;
import com.assessment.demo.entity.Post;
import com.assessment.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class PostDto {
    private UUID postID;
    private String author;
    private String authorImage;
    private Date createdAt;

    private String title;
    private String content;     // This may include media, photo,...

    private int reactCount;
    private int commentCount;
    private int sharedCount;

    private PostStatus status;

    // nullable fields
    private Date updatedAt;
    private String location;    // if the post writer added it into the post

    public static List<PostDto> createPostsList(List<Post> Posts, User user) {
        return Posts.stream()
                .map(post -> PostDto.builder()
                        .author(user.getUsername())
                        .authorImage(user.getImage())
                        .createdAt(post.getCreatedAt())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .reactCount(post.getReacts().size())
                        .commentCount(post.getComments().size())
                        //.sharedCount(post.getSharedCount())
                        .status(post.getStatus())
                        .updatedAt(post.getUpdatedAt())
                        .location(post.getLocation())
                        .build())
                .collect(Collectors.toList());
    }
}
