package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "User")
public class User extends BaseEntity implements UserDetails {              // In progress
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userId", columnDefinition = "BINARY(16)")
    private UUID userId;

    // Foreign Key
    // Unidirectional: user has relationship many-to-one with the role
    @ManyToOne
    @JoinColumn(name = "roleId")
    private Role role;

    // If 1 user is removed, the token of that user will be deleted too
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tokenId", unique = true)
    private Token token;

    // The rest fields
    // 1. Required non-null field
    @Column(name = "first_name", nullable = false, length = 20)
    private String first_name;

    @Column(name = "last_name", nullable = false, length = 20)
    private String last_name;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    // 2. Nullable fields
    @Column(name = "bio", length = 200)
    private String bio;

    @Column(name = "image")
    private String image;

    @Column(name = "Date_of_birth", columnDefinition = "DATE")
    private LocalDate dateOfBirth;

    // More information
    // gender, place, works, hobbies,......

    // Constructors
    public User() {
        // Generate a new UUID for the user during object creation
        super();    // call created_at and updatedAt initialization in the super class
        this.userId = UUID.randomUUID();
    }

    // Should create a new User with follow details in 1 line, not separate to maximize the performance
    public User(String username,String password,String email,String first_name,String last_name,Role role,
                @Nullable String bio,@Nullable String image, @Nullable LocalDate dateOfBirth) {
        // Create user with important details
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isDeleted = false;
        this.status = true;
        // Below is user profile parts
        this.first_name = first_name;
        this.last_name = last_name;
        this.bio = bio;
        this.image = image;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getRoleName()));
    }

    // In this project, account is not expired for sign up again
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // If Admin lock this account, the status will be set equals to false
    @Override
    public boolean isAccountNonLocked(){
        return this.status;
    }

    // Credential will expire when the token is expired, and user need to log in again
    // DEVELOP LATER
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // If the user delete account, isEnable will be set equals to false
    @Override
    public boolean isEnabled() {
        return !this.isDeleted;
    }
}