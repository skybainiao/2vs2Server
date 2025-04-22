package com.example._vs2Server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(columnNames = {"name", "source"})
})
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer source;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;
}