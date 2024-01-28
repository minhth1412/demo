package com.assessment.demo.repository;

import com.assessment.demo.entity.Enum.RequestStatus;
import com.assessment.demo.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, UUID> {
    List<Friend> findBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, RequestStatus status);

}
