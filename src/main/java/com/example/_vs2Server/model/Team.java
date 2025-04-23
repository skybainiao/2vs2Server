package com.example._vs2Server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"groupId", "source"}) // 同一组内数据源唯一
})
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer source;
    private String groupId; // 队伍关联组ID
    private String role;     // 主队/客队标识（home/away）

    @ManyToOne
    @JoinColumn(name = "league_group_id") // 关联同数据源联赛
    private League league;
}