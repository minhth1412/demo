package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.TypeReact;
import com.assessment.demo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "React")
@EqualsAndHashCode(callSuper = true)
public class React extends BaseEntity {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "reactId", columnDefinition = "BINARY(16)")
    private UUID reactId;

    // Foreign keys
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User sender;

    // Non-null fields
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "typeReact", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeReact typeReact;

    // Create 2 tables: react_comment and react_post
    @ManyToMany
    @JoinTable(
            name = "react_comment",
            joinColumns = @JoinColumn(name = "reactId"),
            inverseJoinColumns = @JoinColumn(name = "commentId")
    )
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "react_post",
            joinColumns = @JoinColumn(name = "reactId"),
            inverseJoinColumns = @JoinColumn(name = "postId")
    )
    private Set<Post> posts = new HashSet<>();


    public React() {
        super();
    }

    // Constructors
    public React(UUID id, TypeReact newTypeReact, User user) {
        this();
        this.reactId = id;
        this.status = true;
        this.typeReact = newTypeReact;
        this.sender = user;
    }

    public void updateStatus(Boolean status) {
        this.status = status;
        this.updateDate();
    }

    // This method return: (This should be used in ReactService)
    // + true: this like belongs to a post
    // + false: this like belongs to a comment
    public Boolean belongsToPost() {
        return this.posts != null && !this.posts.isEmpty();
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.getReacts().add(this);
    }

    // Add a method to remove a comment from the set
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.getReacts().remove(this);
    }

    // Add a method to add a post to the set
    public void addPost(Post post) {
        this.posts.add(post);
        post.getReacts().add(this);
    }

    // Add a method to remove a post from the set
    public void removePost(Post post) {
        this.posts.remove(post);
        post.getReacts().remove(this);
    }

    public void updateReaction(TypeReact newTypeReact) {
        this.typeReact = newTypeReact;
        this.updateDate();
    }
}

