package com.example._vs2Server.repository;

import com.example._vs2Server.model.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    boolean existsByGroupIdAndSource(String groupId, Integer source);
    Optional<League> findByGroupIdAndSource(String groupId, Integer source);
}