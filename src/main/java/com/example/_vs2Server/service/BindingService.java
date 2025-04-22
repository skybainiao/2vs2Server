package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.model.League;
import com.example._vs2Server.model.MatchBinding;
import com.example._vs2Server.model.Team;
import com.example._vs2Server.repository.LeagueRepository;
import com.example._vs2Server.repository.MatchBindingRepository;
import com.example._vs2Server.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BindingService {
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final MatchBindingRepository matchBindingRepository;

    @Transactional
    public void saveBinding(BindingRequest request) {
        MatchBinding binding = new MatchBinding();

        processSource(request.getSource1(), binding, 1);
        processSource(request.getSource2(), binding, 2);
        processSource(request.getSource3(), binding, 3);


        matchBindingRepository.save(binding);

    }

    private void processSource(BindingRequest.SourceData sourceData, MatchBinding binding, int sourceNum) {
        // 处理联赛
        League league = leagueRepository.findByExternalIdAndSource(sourceData.getExternalId(), sourceData.getSource())
                .orElseGet(() -> {
                    League newLeague = new League();
                    newLeague.setName(sourceData.getLeagueName());
                    newLeague.setSource(sourceData.getSource());
                    newLeague.setExternalId(sourceData.getExternalId());
                    return leagueRepository.save(newLeague);
                });

        // 处理主队
        Team homeTeam = teamRepository.findByExternalIdAndSource(sourceData.getExternalId() + "_home", sourceData.getSource())
                .orElseGet(() -> {
                    Team newTeam = new Team();
                    newTeam.setName(sourceData.getHomeTeam());
                    newTeam.setSource(sourceData.getSource());
                    newTeam.setExternalId(sourceData.getExternalId() + "_home");
                    return teamRepository.save(newTeam);
                });

        // 处理客队
        Team awayTeam = teamRepository.findByExternalIdAndSource(sourceData.getExternalId() + "_away", sourceData.getSource())
                .orElseGet(() -> {
                    Team newTeam = new Team();
                    newTeam.setName(sourceData.getAwayTeam());
                    newTeam.setSource(sourceData.getSource());
                    newTeam.setExternalId(sourceData.getExternalId() + "_away");
                    return teamRepository.save(newTeam);
                });

        // 设置到binding对象
        switch (sourceNum) {
            case 1:
                binding.setLeague1(league);
                binding.setHomeTeam1(homeTeam);
                binding.setAwayTeam1(awayTeam);
                binding.setMatchTime1(LocalDateTime.parse(sourceData.getMatchTime()));
                binding.setSource1(sourceData.getSource());
                break;
            case 2:
                binding.setLeague2(league);
                binding.setHomeTeam2(homeTeam);
                binding.setAwayTeam2(awayTeam);
                binding.setMatchTime2(LocalDateTime.parse(sourceData.getMatchTime()));
                binding.setSource2(sourceData.getSource());
                break;
            case 3:
                binding.setLeague3(league);
                binding.setHomeTeam3(homeTeam);
                binding.setAwayTeam3(awayTeam);
                binding.setMatchTime3(LocalDateTime.parse(sourceData.getMatchTime()));
                binding.setSource3(sourceData.getSource());
                break;
        }
    }
}
