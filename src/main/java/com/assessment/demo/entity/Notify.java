package com.assessment.demo.entity;

import com.assessment.demo.entity.Enum.RequestStatus;
import com.assessment.demo.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
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
    @JsonIgnore // Ignore the user field during serialization
    private User user;

    // Other fields
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "isRead", nullable = false)
    private Boolean isRead;

    public Notify() {
        super();
    }

    public Notify(User user, String message) {
        this();
        this.notificationId = UUID.randomUUID();
        this.user = user;
        this.message = message;
        this.isRead = false;        // This is new notification, so this is default value
    }

    public void update() {
        this.isRead = true;
        this.setUpdatedAt(new Date());
    }
}
