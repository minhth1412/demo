package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Notify")
public class Notify extends BaseEntity {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "notificationId", columnDefinition = "BINARY(16)")
    private UUID notificationId;

    // Foreign Key
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // Other fields
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "isRead", nullable = false)
    private Boolean isRead;
}
