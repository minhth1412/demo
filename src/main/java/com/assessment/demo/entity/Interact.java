package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.TypeInteract;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "Interact")
public class Interact {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "interactId", columnDefinition = "BINARY(16)")
    private UUID interactId;

    // Foreign keys
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User sender;

    // Non-null fields
    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "typeInteract", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeInteract typeInteract;

    // Create 2 tables: interact_comment and interact_post
    @ManyToMany
    @JoinTable(
            name = "interact_comment",
            joinColumns = @JoinColumn(name = "interactId"),
            inverseJoinColumns = @JoinColumn(name = "commentId")
    )
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "interact_post",
            joinColumns = @JoinColumn(name = "interactId"),
            inverseJoinColumns = @JoinColumn(name = "postId")
    )
    private Set<Post> posts = new HashSet<>();

    // Constructors
    public Interact(TypeInteract typeInteract) {
        // Generate a new UUID for the user during object creation
        super();
        this.interactId = UUID.randomUUID();
        this.status = true;
        this.typeInteract = typeInteract;
    }

    // This method return: (This should be placed in interactService
    // + true: this like belongs to a post
    // + false: this like belongs to a comment
    // public Boolean isInteractWithPost() {return this.post != null;}
}

