package com.assessment.demo.entity.base;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

// implement Serializable because the id is a primitive type
//   which is by default serializable
@Data
public class FriendId implements Serializable {

    private UUID sender;
    private UUID receiver;

    // constructors, equals, hashCode methods
}
