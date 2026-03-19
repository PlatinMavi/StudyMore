package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.codemaxxers.model.enums.RequestStatus;

@Entity
@Table(name = "friend_requests")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void deny() {
        this.status = RequestStatus.DENIED;
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }
}
