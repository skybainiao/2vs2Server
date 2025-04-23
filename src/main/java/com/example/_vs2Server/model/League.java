package com.example._vs2Server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"groupId", "source"}) // 同一组内数据源唯一
})
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;    // 数据源特定名称
    private Integer source; // 数据源标识（1/2/3）
    private String groupId;// 关联组ID（三个数据源共享）
}