package com.example._vs2Server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bindings")
public class Binding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Source 1
    @Column(name = "source1_league")
    private String source1League;
    @Column(name = "source1_home_team")
    private String source1HomeTeam;
    @Column(name = "source1_away_team")
    private String source1AwayTeam;

    // Source 2
    @Column(name = "source2_league")
    private String source2League;
    @Column(name = "source2_home_team")
    private String source2HomeTeam;
    @Column(name = "source2_away_team")
    private String source2AwayTeam;

    // Source 3
    @Column(name = "source3_league")
    private String source3League;
    @Column(name = "source3_home_team")
    private String source3HomeTeam;
    @Column(name = "source3_away_team")
    private String source3AwayTeam;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}