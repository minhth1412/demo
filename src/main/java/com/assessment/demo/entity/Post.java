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
    // The get method and addInteract, removeInteract are
    //  implemented on EntityWithInteracts
    @ManyToMany(mappedBy = "posts")
    private Set<Interact> interacts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    // Constructors
    public Post(){
        super();
    }

    /**
     * Creates a Post object.
     * The post timeStamp is set to the current date.
     * The status default is PUBLIC.
     *
     * @param content The content of the post.
     * @param title The title of the post, and this field is optional.
     * @param image The image, this field can be development into situation: a post has many images (like Facebook) .
     * @param location The optional location added by user.
     * @param author The user who created the post
     *
     */
    public Post(String content,@Nullable String title, @Nullable String image,
                @Nullable String location, User author) {
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