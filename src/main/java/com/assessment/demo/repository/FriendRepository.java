package com.assessment.demo.repository;

import com.assessment.demo.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, UUID> {

}
