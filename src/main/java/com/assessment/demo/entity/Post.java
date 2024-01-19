package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.PostStatus;
import com.assessment.demo.entity.base.BaseEntity;
import com.assessment.demo.entity.base.EntityWithInteracts;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Post")
public class Post extends BaseEntity implements EntityWithInteracts {     // In progress
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "postId", columnDefinition = "BINARY(16)")
    private UUID postId;

    // Foreign key
    // This is equals to primary key userId in User table
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    //@Column(name = "author", nullable = false)
    private User author;

    // nullable fields
    @Column(name = "title")
    private String title;

    // non-nullable fields
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @ManyToOne
    @JoinColumn(name = "repostFrom")
    private Post repostFrom;

    // Handle foreign keys
    // The get method and addInteract, removeInteract are
    //  implemented on EntityWithInteracts
    @ManyToMany(mappedBy = "posts")
    private Set<Interact> interacts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    // Constructors
    // This is for common use
    public Post(String content,@Nullable String title, User author, @Nullable Post mainPost) {
        // Generate these when calling new Post()
        super();
        this.postId = UUID.randomUUID();
        this.updateStatus(PostStatus.PUBLIC);
//        // Validate parameters
//        if (content == null || author == null) {
//            throw new IllegalArgumentException("Content or author must not be null!");
//        }
        this.content = content;
        this.title = title;
        this.author = author;
        this.repostFrom = mainPost;
    }

    // Method for update post status, include Delete post
    public void updateStatus(PostStatus status) {
        this.status = status;
        this.updateDate();
    }

    // Override from entityWithInteracts
    @Override
    public Set<Interact> getInteracts() {
        return interacts;
    }
    // ...(later )
}