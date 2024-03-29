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

    // nullable fields--------------
    private Date updatedAt;
    private String location;    // if the post writer added it into the post
    // ------------------------------
    private PostStatus status;
    private String title;
    private String content;     // This may include media, photo,...

    private int reactCount;
    private int commentCount;
    private int sharedCount;


    public static PostDto createPostDto(Post post, User user) {
        return createPostDto(post,user.getUsername(),user.getImage());
    }

    public static PostDto createPostDto(Post post, String author, String authorImage) {
        return PostDto.builder()
                .postID(post.getPostId())
                .author(author)
                .authorImage(authorImage)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .location(post.getLocation())
                .status(post.getStatus())
                .title(post.getTitle())
                .content(post.getContent())
                .reactCount(post.getReacts().size())
                .commentCount(post.getComments().size())
                //.sharedCount(post.getSharedCount())
                .build();
    }

    public static List<PostDto> createPostsList(List<Post> Posts, User user) {
        return Posts.stream()
                .map(post -> createPostDto(post, user))
                .collect(Collectors.toList());
    }
    public static PostDto createPostDto(Post post) {
        return PostDto.builder()
                .postID(post.getPostId())
                .author(post.getAuthor().getUsername())
                .authorImage(post.getAuthor().getImage())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .location(post.getLocation())
                .status(post.getStatus())
                .title(post.getTitle())
                .content(post.getContent())
                .reactCount(post.getReacts().size())
                .commentCount(post.getComments().size())
                //.sharedCount(post.getSharedCount())
                .build();
    }

    public static List<PostDto> createPostsList(List<Post> Posts) {
        return Posts.stream()
                .map(PostDto::createPostDto)
                .collect(Collectors.toList());
    }

}
