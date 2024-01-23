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
@Table(name = "SharedPost")
public class SharedPost extends BaseEntity {     // In progress
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sharedPostId", columnDefinition = "BINARY(16)")
    private UUID sharedPostId;

    // Foreign key
    // This is equals to primary key userId in User table
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Post postId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User author;

    // nullable fields
    @Column(name = "content")
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    // isDeleted = False in default, check if this comment is deleted or not,
    //  but the data will stay in the db whatever the situation
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    // Handle foreign keys
    // The get method and addInteract, removeInteract are
    //  implemented on EntityWithInteracts

//    @ManyToMany(mappedBy = "SharedPosts")
//    private Set<Interact> interacts = new HashSet<>();
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "SharedPost")
//    private List<Comment> comments = new ArrayList<>();

    // Constructors
    public SharedPost(){
        super();
    }

    // This is for common use
    public SharedPost(String content,@Nullable String title,
                      User author) {
        // Generate these when calling new Post()
        super();
        this.sharedPostId = UUID.randomUUID();
        this.updateStatus(PostStatus.PUBLIC);
//        // Validate parameters
//        if (content == null || author == null) {
//            throw new IllegalArgumentException("Content or author must not be null!");
//        }
        this.content = content;
        this.isDeleted = false;
    }

    // Method for update post status, include Delete post
    public void updateStatus(PostStatus status) {
        this.status = status;
        this.updateDate();
    }
}