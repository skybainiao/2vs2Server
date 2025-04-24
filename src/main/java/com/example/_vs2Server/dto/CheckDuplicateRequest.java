package com.example._vs2Server.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckDuplicateRequest {
    private int source; // 1,2,3 表示数据源
    private List<MatchData> matches;

    @Data
    public static class MatchData {
        private String league;
        private String homeTeam;
        private String awayTeam;
    }
}
