package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.PostStatus;
import com.assessment.demo.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Post")
public class Post extends BaseEntity {     // In progress
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "postId", columnDefinition = "BINARY(16)")
    private UUID postId;

    // Foreign key
    // This is equals to primary key userId in User table
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User author;

    // nullable fields
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(name = "content", nullable = false)
    private String content;

    // non-nullable fields
    @Column(name = "image")
    private String image;

    @Column(name = "title")
    private String title;

    // isDeleted = False in default, check if this comment is deleted or not,
    //  but the data will stay in the db whatever the situation
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "location")
    private String location;

    // Handle foreign keys
    // The get method and addReact, removeReact are
    //  implemented on EntityWithReacts
    @ManyToMany(mappedBy = "posts")
    private Set<React> reacts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post")
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "originalPostId")
    @JsonIgnore
    private Post originalPost;

    // Shared posts relationship
    @OneToMany(mappedBy = "originalPost", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Post> sharedPosts = new ArrayList<>();

    // Constructors
    public Post(){
        super();
    }

    public Post(String content,@Nullable String title, @Nullable String image,
                @Nullable String location, User author){//, @Nullable UUID parentPost) {
        // Generate these when calling new Post()
        super();
        this.postId = UUID.randomUUID();
        this.updateStatus(PostStatus.PUBLIC);
        this.content = content;
        this.title = title;
        this.author = author;
        this.isDeleted = false;
        this.image = image;
        this.location = location;
        this.originalPost = null;
    }

    // Method for update post status, include Delete post
    public void updateStatus(PostStatus status) {
        this.status = status;
        this.updateDate();
    }

    public Post sharePost(User author) {
        Post sharedPost = new Post(this.content, this.title, this.image, this.location, author);
        sharedPost.setOriginalPost(this);
        sharedPost.updateStatus(PostStatus.PUBLIC);
        this.sharedPosts.add(sharedPost);
        return sharedPost;
    }

    public void addComment(Comment comment) {
        this.getComments().add(comment);
        comment.setPost(this);
    }

    // Remove a Like from the <EntityWithReacts>
    // The like will be saved in db instead of deleting for tracking history purpose
    public void removeComment(Comment comment) {
        this.getComments().remove(comment);
        comment.setIsDeleted(true);
    }
}