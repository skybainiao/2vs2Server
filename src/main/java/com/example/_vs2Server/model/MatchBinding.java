package com.example._vs2Server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MatchBinding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "league1_id")
    private League league1;
    @ManyToOne
    @JoinColumn(name = "home_team1_id")
    private Team homeTeam1;
    @ManyToOne
    @JoinColumn(name = "away_team1_id")
    private Team awayTeam1;
    private Integer source1;

    @ManyToOne
    @JoinColumn(name = "league2_id")
    private League league2;
    @ManyToOne
    @JoinColumn(name = "home_team2_id")
    private Team homeTeam2;
    @ManyToOne
    @JoinColumn(name = "away_team2_id")
    private Team awayTeam2;
    private Integer source2;

    @ManyToOne
    @JoinColumn(name = "league3_id")
    private League league3;
    @ManyToOne
    @JoinColumn(name = "home_team3_id")
    private Team homeTeam3;
    @ManyToOne
    @JoinColumn(name = "away_team3_id")
    private Team awayTeam3;
    private Integer source3;

    private LocalDateTime createdAt = LocalDateTime.now();
}