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

import java.util.Optional;

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
        if (sourceData == null) {
            return;
        }

        League league = getOrCreateLeague(sourceData.getLeagueName(), sourceData.getSource());

        Team homeTeam = getOrCreateTeam(sourceData.getHomeTeam(), sourceData.getSource(), league);
        Team awayTeam = getOrCreateTeam(sourceData.getAwayTeam(), sourceData.getSource(), league);

        switch (sourceNum) {
            case 1:
                binding.setLeague1(league);
                binding.setHomeTeam1(homeTeam);
                binding.setAwayTeam1(awayTeam);
                binding.setSource1(sourceData.getSource());
                break;
            case 2:
                binding.setLeague2(league);
                binding.setHomeTeam2(homeTeam);
                binding.setAwayTeam2(awayTeam);
                binding.setSource2(sourceData.getSource());
                break;
            case 3:
                binding.setLeague3(league);
                binding.setHomeTeam3(homeTeam);
                binding.setAwayTeam3(awayTeam);
                binding.setSource3(sourceData.getSource());
                break;
        }
    }

    private League getOrCreateLeague(String name, Integer source) {
        return leagueRepository.findByNameAndSource(name, source)
                .orElseGet(() -> {
                    League league = new League();
                    league.setName(name);
                    league.setSource(source);
                    return leagueRepository.save(league);
                });
    }

    private Team getOrCreateTeam(String name, Integer source, League league) {
        return teamRepository.findByNameAndSource(name, source)
                .orElseGet(() -> {
                    Team team = new Team();
                    team.setName(name);
                    team.setSource(source);
                    team.setLeague(league);
                    return teamRepository.save(team);
                });
    }
}