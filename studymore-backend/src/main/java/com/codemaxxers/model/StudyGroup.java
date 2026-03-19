package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

@Entity
@Table(name = "study_groups")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String studyGoal;

    private int maxMembers;
    
    private boolean isActive;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = true;
    }

    public void addMember(User user) {
        if (members.size() < maxMembers) {
            members.add(user);
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public boolean isFull() {
        return members.size() >= maxMembers;
    }

    public List<User> getLeaderboard() {
        List<User> leaderboard = new ArrayList<>(members);

        // Collections.sort(leaderboard, new Comparator<User>() {
        //     @Override
        //     // public int compare(User user1, User user2) {
        //         // return Integer.compare(user2.getRating(), user1.getRating()); // TODO
        //     }
        // });

        return leaderboard;
    }
}