package com.assessment.demo.service.impl;

import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Enum.RequestStatus;
import com.assessment.demo.entity.Friend;
import com.assessment.demo.entity.Notify;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.FriendRepository;
import com.assessment.demo.repository.NotifyRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final FriendRepository friendRepository;
    private final NotifyRepository notifyRepository;
    private final UserRepository userRepository;

    private boolean areUsersHasStatus(User user1, User user2, RequestStatus status) {
        // Check if user1 and user2 have status
        List<Friend> friends = friendRepository.findBySenderAndReceiverAndStatus(
                user1, user2, status);
        return !friends.isEmpty();
    }

    @Override
    public UsualResponse sendRequest(User requester, UUID receiver) {
        try {
            User receiveUser = userRepository.findByUserId(receiver).orElse(null);
            if (receiveUser == null)
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "User not found!");

            if (areUsersHasStatus(requester, receiveUser, RequestStatus.ACCEPTED))
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "You two have already been friend of each other before!");

            if (areUsersHasStatus(requester, receiveUser, RequestStatus.PENDING))
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "You have already send friend request to this user!");

            Friend friendRequest = new Friend(requester, receiveUser);
            friendRepository.save(friendRequest);
            Notify notification = new Notify(receiveUser, "You have a friend request from " + requester.getUsername());
            notifyRepository.save(notification);
            return UsualResponse.success("Friend request sent successfully");
        } catch (Exception e) {
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while sending friend request.");
        }
    }

    @Override
    public UsualResponse acceptFriendRequest(User userAccept, UUID userSendRequest) {
        try {
            User requester = userRepository.findByUserId(userSendRequest).orElse(null);
            if (requester == null)
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "User not found!");

            if (areUsersHasStatus(requester, userAccept, RequestStatus.ACCEPTED))
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "You two have already been friend of each other before!");

            else if (areUsersHasStatus(requester, userAccept, RequestStatus.PENDING)) {
                Friend friendRequest = friendRepository.findBySenderAndReceiverAndStatus(
                        requester, userAccept, RequestStatus.PENDING).get(0);
                friendRequest.updateStatus(RequestStatus.ACCEPTED);
                friendRepository.save(friendRequest);

                Notify notification = new Notify(requester, "You and " + requester.getUsername() + " now are friends!");
                notifyRepository.save(notification);
                return UsualResponse.success("Friend request sent successfully");
            } else return UsualResponse.error(HttpStatus.BAD_REQUEST, "Friend request not found!");

        } catch (Exception e) {
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while retrieving the accept friendship.");
        }
    }

    @Override
    public UsualResponse rejectFriendRequest(User userReject, UUID userSendRequest) {
        try {
            User requester = userRepository.findByUserId(userSendRequest).orElse(null);
            if (requester == null)
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "User not found!");

            if (areUsersHasStatus(requester, userReject, RequestStatus.ACCEPTED))
                return UsualResponse.error(HttpStatus.BAD_REQUEST,
                        "You two have already been friend of each other before!");

            else if (areUsersHasStatus(requester, userReject, RequestStatus.PENDING)) {
                Friend friendRequest = friendRepository.findBySenderAndReceiverAndStatus(
                        requester, userReject, RequestStatus.PENDING).get(0);
                friendRequest.updateStatus(RequestStatus.DENIED);
                friendRepository.save(friendRequest);

                Notify notification = new Notify(requester, "You rejected friend request from " + requester.getUsername());
                notifyRepository.save(notification);
                return UsualResponse.success("Friend request rejected");
            } else return UsualResponse.error(HttpStatus.BAD_REQUEST, "Friend request not found!");

        } catch (Exception e) {
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while retrieving the reject friendship.");
        }
    }

//    @Override
//    public UsualResponse removeFriend(User requester, UUID receiver) {
//        ..................
//        return UsualResponse.success("Friend removed successfully");
//    }
}
