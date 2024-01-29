package com.assessment.demo.entity.base;

import com.assessment.demo.entity.React;

import java.util.Set;

public interface EntityWithReacts {
    Set<React> getReacts(); // Getter for the reacts set

    // Add a Like to the <EntityWithReacts>
    // No need to identify the commentId in Like Entity,
    // Just call <EntityWithReacts>.addLike;
    default void addReact(React react) {
        getReacts().add(react);
        react.setStatus(false);
    }

    // Remove a Like from the <EntityWithReacts>
    // The like will be saved in db instead of deleting for tracking history purpose
    default void removeReact(React react) {
        getReacts().remove(react);
        react.setStatus(false);
    }
}