package com.codemaxxers.repository;

import com.codemaxxers.model.StudyGroup;
import com.codemaxxers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    List<StudyGroup> findByIsActiveTrue(); 
    List<StudyGroup> findByHost(User host);
}