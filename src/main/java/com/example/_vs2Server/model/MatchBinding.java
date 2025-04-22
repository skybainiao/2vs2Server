package com.example._vs2Server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MatchBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private League league1;
    @ManyToOne
    private Team homeTeam1;
    @ManyToOne
    private Team awayTeam1;
    private LocalDateTime matchTime1;
    private Integer source1;

    @ManyToOne
    private League league2;
    @ManyToOne
    private Team homeTeam2;
    @ManyToOne
    private Team awayTeam2;
    private LocalDateTime matchTime2;
    private Integer source2;

    @ManyToOne
    private League league3;
    @ManyToOne
    private Team homeTeam3;
    @ManyToOne
    private Team awayTeam3;
    private LocalDateTime matchTime3;
    private Integer source3;

    private LocalDateTime createdAt = LocalDateTime.now();
}
