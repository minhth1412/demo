package com.assessment.demo.entity.base;

import com.assessment.demo.entity.Interact;

import java.util.HashSet;
import java.util.Set;

public interface EntityWithInteracts {
    Set<Interact> getInteracts(); // Getter for the interacts set

    // Add a Like to the <EntityWithInteracts>
    // No need to identify the commentId in Like Entity,
    // Just call <EntityWithInteracts>.addLike;
    default void addInteract(Interact interact) {
        getInteracts().add(interact);
        interact.setStatus(false);
    }

    // Remove a Like from the <EntityWithInteracts>
    // The like will be saved in db instead of deleting for tracking history purpose
    default void removeInteract(Interact interact) {
        getInteracts().remove(interact);
        interact.setStatus(false);
    }
}