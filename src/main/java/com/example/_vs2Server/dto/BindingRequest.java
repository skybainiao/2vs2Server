package com.example._vs2Server.dto;

import lombok.Data;

@Data
public class BindingRequest {
    @Data
    public static class SourceData {
        private String leagueName;
        private String homeTeam;
        private String awayTeam;
        private Integer source;

    }

    private SourceData source1;
    private SourceData source2;
    private SourceData source3;
}