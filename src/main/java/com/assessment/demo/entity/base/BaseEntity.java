package com.assessment.demo.entity.base;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt ;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public BaseEntity(){
//        id = UUID.randomUUID();
        createdAt  = new Date();
        updatedAt = createdAt ;
    }

    public void updateDate() {
        updatedAt = new Date();
    }
}
