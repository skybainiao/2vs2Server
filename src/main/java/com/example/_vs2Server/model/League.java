package com.example._vs2Server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(columnNames = {"name", "source"})
})
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer source;

    @OneToMany(mappedBy = "league")
    private List<Team> teams = new ArrayList<>();
}