package com.assessment.demo.entity.base;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@MappedSuperclass
@Data
public abstract class BaseEntity {

    @Column(name = "createdAt", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private final Date createdAt ;

    @Column(name = "updatedAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date updatedAt;

    public BaseEntity(){
//        id = UUID.randomUUID();
        createdAt  = new Date();
        updatedAt = createdAt ;
    }

    public void updateDate() {
        updatedAt = new Date();
    }
}
