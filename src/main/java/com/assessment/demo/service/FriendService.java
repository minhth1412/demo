package com.assessment.demo.service;

import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.entity.User;

import java.util.UUID;

public interface FriendService {

    UsualResponse sendRequest(User requester, UUID receiver);

    UsualResponse acceptFriendRequest(User userAccept, UUID userSendRequest);

    UsualResponse rejectFriendRequest(User userReject, UUID userSendRequest);

    // Will be deployed later
    //UsualResponse removeFriend(User requester, UUID receiver);
}
