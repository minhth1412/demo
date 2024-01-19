package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import com.assessment.demo.entity.base.EntityWithInteracts;
import io.micrometer.common.lang.Nullable;
import lombok.Data;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Comment")
public class Comment extends BaseEntity implements EntityWithInteracts {     // DONE temporary
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

    @OneToOne
    @JoinColumn(name = "author")
    private User author;

    // isDeleted = False in default, check if this comment is deleted or not,
    //  but the data will stay in the db whatever the situation
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    // Nullable fields
    @Column(name = "content")
    private String content;

    // Manage foreign Key, the get method and addInteract, removeInteract are
    //  implemented on EntityWithInteracts
    @ManyToMany(mappedBy = "comments")
    private Set<Interact> interacts = new HashSet<>();

    // Constructors
    public Comment(Post post, @Nullable Comment replyTo) {
        // Generate a new UUID for the user during object creation
        super();
        this.commentId = UUID.randomUUID();
        this.isDeleted = false;
        this.replyTo = replyTo;         // Even equals null
        this.post = post;
    }


    // Override from entityWithInteracts
    @Override
    public Set<Interact> getInteracts() {
        return interacts;
    }
    // ...(later )
}