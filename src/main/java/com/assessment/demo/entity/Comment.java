package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import io.micrometer.common.lang.Nullable;
import lombok.Data;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Comment")
public class Comment extends BaseEntity {     // DONE temporary
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "commentId", columnDefinition = "BINARY(16)")
    private UUID commentId;

    // Foreign Key
    @ManyToOne
    @JoinColumn(name = "postId")
    private Post post;      // This maps into the Post entity

    // This is not null when replied to a comment in the postId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reply_to")
    private Comment replyTo;

    @ManyToOne
    @JoinColumn(name = "author")
    private User author;

    // isDeleted = False in default, check if this comment is deleted or not,
    //  but the data will stay in the db whatever the situation
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    // Nullable fields
    @Column(name = "content")
    private String content;

    // Override from entityWithReacts
    // Manage foreign Key, the get method and addReact, removeReact are
    //  implemented on EntityWithReacts
    @ManyToMany(mappedBy = "comments")
    private Set<React> reacts = new HashSet<>();

    public Comment() {
        super();
        this.commentId = UUID.randomUUID();
    }

    // Constructors
    public Comment(Post post, @Nullable Comment replyTo, String content, User user) {
        // Generate a new UUID for the user during object creation
        this();
        this.replyTo = replyTo;         // Even equals null
        this.post = post;
        this.content = content;
        this.author = user;
    }

}