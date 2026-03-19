package com.codemaxxers.repository;

import com.codemaxxers.model.FriendRequest;
import com.codemaxxers.model.User;
import com.codemaxxers.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverAndStatus(User receiver, RequestStatus status);
    boolean existsBySenderAndReceiver(User sender, User receiver);
}