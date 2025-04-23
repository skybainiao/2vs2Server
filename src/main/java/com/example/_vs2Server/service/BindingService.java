package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.model.League;

import com.example._vs2Server.model.Team;
import com.example._vs2Server.repository.LeagueRepository;

import com.example._vs2Server.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BindingService {
    private final LeagueRepository leagueRepo;
    private final TeamRepository teamRepo;

    @Transactional
    public void saveBinding(BindingRequest request) { // 改为接收单个请求
        // 生成唯一组ID（示例用UUID，实际可用哈希）
        String leagueGroupId = UUID.randomUUID().toString();
        String homeTeamGroupId = UUID.randomUUID().toString();
        String awayTeamGroupId = UUID.randomUUID().toString();

        // 处理三个数据源的联赛（移除外层循环）
        processLeague(request.getSource1(), 1, leagueGroupId);
        processLeague(request.getSource2(), 2, leagueGroupId);
        processLeague(request.getSource3(), 3, leagueGroupId);

        // 处理主客队（移除外层循环）
        processTeam(request.getSource1().getHomeTeam(), 1, homeTeamGroupId, leagueGroupId, "home");
        processTeam(request.getSource2().getHomeTeam(), 2, homeTeamGroupId, leagueGroupId, "home");
        processTeam(request.getSource3().getHomeTeam(), 3, homeTeamGroupId, leagueGroupId, "home");

        processTeam(request.getSource1().getAwayTeam(), 1, awayTeamGroupId, leagueGroupId, "away");
        processTeam(request.getSource2().getAwayTeam(), 2, awayTeamGroupId, leagueGroupId, "away");
        processTeam(request.getSource3().getAwayTeam(), 3, awayTeamGroupId, leagueGroupId, "away");
    }

    private void processLeague(BindingRequest.SourceData data, int source, String groupId) {
        if (data == null) return;

        // 检查是否已存在同组同源的联赛
        if (!leagueRepo.existsByGroupIdAndSource(groupId, source)) {
            League league = new League();
            league.setName(data.getLeagueName());
            league.setSource(source);
            league.setGroupId(groupId);
            leagueRepo.save(league);
        }
    }

    private void processTeam(String teamName, int source,
                             String teamGroupId, String leagueGroupId, String role) {
        if (teamName == null) return;

        // 检查是否已存在同组同源的队伍
        if (!teamRepo.existsByGroupIdAndSource(teamGroupId, source)) {
            // 获取同数据源的联赛
            League league = leagueRepo.findByGroupIdAndSource(leagueGroupId, source)
                    .orElseThrow(() -> new RuntimeException("联赛未找到"));

            Team team = new Team();
            team.setName(teamName);
            team.setSource(source);
            team.setGroupId(teamGroupId);
            team.setRole(role);
            team.setLeague(league);
            teamRepo.save(team);
        }
    }
}
