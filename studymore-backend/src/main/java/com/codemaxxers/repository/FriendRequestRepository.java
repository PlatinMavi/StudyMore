package com.codemaxxers.repository;

import com.codemaxxers.model.FriendRequest;
import com.codemaxxers.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiver_IdAndStatus(Long receiverId, RequestStatus status);
    List<FriendRequest> findBySender_Id(Long senderId);
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "(fr.sender.id = :a AND fr.receiver.id = :b) OR " +
           "(fr.sender.id = :b AND fr.receiver.id = :a)")
    Optional<FriendRequest> findBetweenUsers(@Param("a") Long userAId, @Param("b") Long userBId);
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
           "fr.status = 'ACCEPTED' AND " +
           "(fr.sender.id = :userId OR fr.receiver.id = :userId)")
    List<FriendRequest> findAcceptedByUserId(@Param("userId") Long userId);
}