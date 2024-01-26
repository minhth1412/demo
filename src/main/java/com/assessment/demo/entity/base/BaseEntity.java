package com.assessment.demo.entity.base;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "createdAt", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt = new Date();

    @Column(name = "updatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public void updateDate() {
        updatedAt = new Date();
    }

    public BaseEntity() {
        this.updatedAt = null;
    }
}
