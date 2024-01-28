package com.assessment.demo.service.impl;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;

public class FriendServiceImpl implements FriendService {

    @Override
    public UsualResponse sendRequest(HttpServletRequest request) {


        return UsualResponse.success("Friend request sent successfully");
    }

    @Override
    public UsualResponse acceptFriendRequest(HttpServletRequest request) {


        return UsualResponse.success("Friend request accepted");
    }

    @Override
    public UsualResponse rejectFriendRequest(HttpServletRequest request) {


        return UsualResponse.success("Friend request rejected");
    }

    @Override
    public UsualResponse removeFriend(HttpServletRequest request) {


        return UsualResponse.success("Friend removed successfully");
    }
}
