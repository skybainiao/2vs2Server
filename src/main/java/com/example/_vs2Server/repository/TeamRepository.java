package com.example._vs2Server.repository;

import com.example._vs2Server.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByGroupIdAndSource(String groupId, Integer source);
}