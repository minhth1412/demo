package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.TypeReact;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "React")
public class React {
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
    private Boolean status;

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

    // Constructors
    public React(String typeReact) {
        // Generate a new UUID for the user during object creation
        super();
        this.reactId = UUID.randomUUID();
        this.status = true;
        this.typeReact = TypeReact.valueOf(typeReact);
    }

    // This method return: (This should be placed in ReactService
    // + true: this like belongs to a post
    // + false: this like belongs to a comment
    // public Boolean isReactWithPost() {return this.post != null;}
}

