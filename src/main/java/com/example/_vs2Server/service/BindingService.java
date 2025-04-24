package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.dto.CheckDuplicateRequest;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.repository.BindingRepository;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BindingService {

    private final BindingRepository bindingRepository;
    private static final Set<String> VALID_COLUMNS = Set.of(
            "source1_league", "source1_home_team", "source1_away_team",
            "source2_league", "source2_home_team", "source2_away_team",
            "source3_league", "source3_home_team", "source3_away_team"
    );

    @PersistenceContext
    private EntityManager entityManager;

    public BindingService(BindingRepository bindingRepository) {
        this.bindingRepository = bindingRepository;
    }

    public void saveBindings(List<BindingRequest> requests) {
        requests.forEach(request -> {
            Binding binding = new Binding();

            // Source 1
            binding.setSource1League(request.getSource1().getLeagueName());
            binding.setSource1HomeTeam(request.getSource1().getHomeTeam());
            binding.setSource1AwayTeam(request.getSource1().getAwayTeam());

            // Source 2
            binding.setSource2League(request.getSource2().getLeagueName());
            binding.setSource2HomeTeam(request.getSource2().getHomeTeam());
            binding.setSource2AwayTeam(request.getSource2().getAwayTeam());

            // Source 3
            binding.setSource3League(request.getSource3().getLeagueName());
            binding.setSource3HomeTeam(request.getSource3().getHomeTeam());
            binding.setSource3AwayTeam(request.getSource3().getAwayTeam());

            bindingRepository.save(binding);
        });
    }

    public List<Binding> getAllBindings() {
        return bindingRepository.findAll();
    }

    public Map<String, Set<String>> checkDuplicates(CheckDuplicateRequest request) {
        int source = request.getSource();
        String prefix = "source" + source;

        String leagueColumn = prefix + "_league";
        String homeColumn = prefix + "_home_team";
        String awayColumn = prefix + "_away_team";

        validateColumn(leagueColumn);
        validateColumn(homeColumn);
        validateColumn(awayColumn);

        // 收集需要检查的数据
        Set<String> leagues = request.getMatches().stream()
                .map(CheckDuplicateRequest.MatchData::getLeague)
                .collect(Collectors.toSet());

        Set<String> teams = request.getMatches().stream()
                .flatMap(m -> Stream.of(m.getHomeTeam(), m.getAwayTeam()))
                .collect(Collectors.toSet());

        return Map.of(
                "leagues", findExistingValues(leagueColumn, leagues),
                "teams", findTeamDuplicates(homeColumn, awayColumn, teams)
        );
    }

    private Set<String> findExistingValues(String column, Set<String> values) {
        if (values.isEmpty()) return Collections.emptySet();

        String sql = String.format(
                "SELECT DISTINCT %s FROM bindings WHERE %s IN (:values)",
                column, column
        );

        return new HashSet<>(entityManager.createNativeQuery(sql)
                .setParameter("values", values)
                .getResultList());
    }

    private Set<String> findTeamDuplicates(String homeColumn, String awayColumn, Set<String> teams) {
        Set<String> result = new HashSet<>();
        if (!teams.isEmpty()) {
            result.addAll(findExistingValues(homeColumn, teams));
            result.addAll(findExistingValues(awayColumn, teams));
        }
        return result;
    }

    private void validateColumn(String column) {
        if (!VALID_COLUMNS.contains(column)) {
            throw new IllegalArgumentException("无效的数据库列名: " + column);
        }
    }
}