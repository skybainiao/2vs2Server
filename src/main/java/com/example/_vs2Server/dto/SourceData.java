package com.example._vs2Server.dto;

import lombok.Data;

@Data
public class SourceData {
    private String leagueName;
    private String homeTeam;
    private String awayTeam;
    private int source;
}