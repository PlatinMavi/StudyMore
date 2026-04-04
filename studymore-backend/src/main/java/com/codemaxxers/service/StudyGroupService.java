package com.codemaxxers.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codemaxxers.model.StudyGroup;
import com.codemaxxers.model.User;
import com.codemaxxers.repository.StudyGroupRepository;
import com.codemaxxers.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class StudyGroupService {
 
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
 
    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             UserRepository userRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }
 
    //  CRUD
    public StudyGroup createGroup(String title, String studyGoal, Long hostId, int maxMembers) {
        User host = findUser(hostId);
        StudyGroup group = new StudyGroup(title, studyGoal, host, maxMembers);
        return studyGroupRepository.save(group);
    }
 
    @Transactional(readOnly = true)
    public StudyGroup getGroup(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }
 
    @Transactional(readOnly = true)
    public List<StudyGroup> getActiveGroups() {
        return studyGroupRepository.findByIsActiveTrue();
    }
 
    @Transactional(readOnly = true)
    public List<StudyGroup> searchGroups(String keyword) {
        return studyGroupRepository.searchActiveByTitle(keyword);
    }
 
    @Transactional(readOnly = true)
    public List<StudyGroup> getGroupsForUser(Long userId) {
        return studyGroupRepository.findActiveGroupsByMemberId(userId);
    }
 
    public void disbandGroup(Long groupId, Long requestingUserId) {
        StudyGroup group = getGroup(groupId);
        if (!group.getHost().getUserId().equals(requestingUserId)) {
            throw new SecurityException("Only the host can disband the group.");
        }
        group.setActive(false);
        studyGroupRepository.save(group);
    }
 
    public StudyGroup joinGroup(Long groupId, Long userId) {
        StudyGroup group = getGroup(groupId);
        User user = findUser(userId);
        group.addMember(user);
        StudyGroup saved = studyGroupRepository.save(group);
        broadcastLeaderboard(saved);
        return saved;
    }
 
    public StudyGroup leaveGroup(Long groupId, Long userId) {
        StudyGroup group = getGroup(groupId);
        User user = findUser(userId);
        group.removeMember(user);
        StudyGroup saved = studyGroupRepository.save(group);
        if (saved.isActive()) broadcastLeaderboard(saved);
        return saved;
    }
 
    // leaderboard
    @Transactional(readOnly = true)
    public List<User> getLeaderboard(Long groupId) {
        return getGroup(groupId).getLeaderboard();
    }
 
    
    public void broadcastLeaderboard(StudyGroup group) {
        String destination = "/topic/group/" + group.getGroupId() + "/leaderboard";

        StringBuilder sb = new StringBuilder("[");
        List<User> leaderboard = group.getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            User u = leaderboard.get(i);
            sb.append("{")
            .append("\"userId\":").append(u.getUserId()).append(",")
            .append("\"username\":\"").append(u.getUsername()).append("\",")
            .append("\"coinBalance\":").append(u.getCoinBalance()).append(",")
            .append("\"rank\":\"").append(u.getRank().name()).append("\"")
            .append("}");
            if (i < leaderboard.size() - 1) sb.append(",");
        }
        sb.append("]");

        messagingTemplate.convertAndSend(destination, sb.toString());
    }
    // helpers
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}