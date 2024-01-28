package com.assessment.demo.service;

import com.assessment.demo.dto.response.others.UsualResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface FriendService {

    UsualResponse sendRequest(HttpServletRequest request);

    UsualResponse acceptFriendRequest(HttpServletRequest request);

    UsualResponse rejectFriendRequest(HttpServletRequest request);

    UsualResponse removeFriend(HttpServletRequest request);
}
