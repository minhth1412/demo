package com.assessment.demo.repository;

import com.assessment.demo.entity.Enum.RequestStatus;
import com.assessment.demo.entity.Friend;
import com.assessment.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, UUID> {
    List<Friend> findBySenderAndReceiverAndStatus(User sender, User receiver, RequestStatus status);

}
