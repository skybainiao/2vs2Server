package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.dto.CheckDuplicateRequest;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.repository.BindingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BindingService {

    private final BindingRepository bindingRepository;
    private static final Set<String> VALID_FIELDS = Set.of(
            "source1League", "source1HomeTeam", "source1AwayTeam",
            "source2League", "source2HomeTeam", "source2AwayTeam",
            "source3League", "source3HomeTeam", "source3AwayTeam"
    );

    @PersistenceContext
    private EntityManager entityManager;

    public BindingService(BindingRepository bindingRepository) {
        this.bindingRepository = bindingRepository;
    }

    @Transactional
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

    public Map<String, Object> checkDuplicates(CheckDuplicateRequest request) {
        int source = request.getSource();
        validateSource(source);

        String leagueField = "source" + source + "League";
        String homeField = "source" + source + "HomeTeam";
        String awayField = "source" + source + "AwayTeam";

        validateField(leagueField);
        validateField(homeField);
        validateField(awayField);

        Set<String> leagues = request.getMatches().stream()
                .map(CheckDuplicateRequest.MatchData::getLeague)
                .collect(Collectors.toSet());

        Set<String> teams = request.getMatches().stream()
                .flatMap(m -> Stream.of(m.getHomeTeam(), m.getAwayTeam()))
                .collect(Collectors.toSet());

        return Map.of(
                "leagues", findExistingValues(leagueField, leagues),
                "teams", findTeamDuplicates(homeField, awayField, teams)
        );
    }

    @Transactional
    public void deleteTeam(int source, String league, String team, String teamType) {
        List<Binding> relatedBindings = findRelatedBindings(source, league, team, teamType);
        for (Binding binding : relatedBindings) {
            if ("home".equals(teamType)) {
                binding.setSource1HomeTeam(null);
                binding.setSource2HomeTeam(null);
                binding.setSource3HomeTeam(null);
            } else if ("away".equals(teamType)) {
                binding.setSource1AwayTeam(null);
                binding.setSource2AwayTeam(null);
                binding.setSource3AwayTeam(null);
            }
        }
        bindingRepository.saveAll(relatedBindings);
        entityManager.flush();
    }

    @Transactional(readOnly = true)
    public List<Binding> findRelatedBindings(int source, String league, String team, String teamType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Binding> query = cb.createQuery(Binding.class);
        Root<Binding> root = query.from(Binding.class);

        Predicate leaguePredicate = null;
        Predicate teamPredicate = null;

        switch (source) {
            case 1:
                leaguePredicate = cb.equal(root.get("source1League"), league);
                if ("home".equals(teamType)) {
                    teamPredicate = cb.equal(root.get("source1HomeTeam"), team);
                } else {
                    teamPredicate = cb.equal(root.get("source1AwayTeam"), team);
                }
                break;
            case 2:
                leaguePredicate = cb.equal(root.get("source2League"), league);
                if ("home".equals(teamType)) {
                    teamPredicate = cb.equal(root.get("source2HomeTeam"), team);
                } else {
                    teamPredicate = cb.equal(root.get("source2AwayTeam"), team);
                }
                break;
            case 3:
                leaguePredicate = cb.equal(root.get("source3League"), league);
                if ("home".equals(teamType)) {
                    teamPredicate = cb.equal(root.get("source3HomeTeam"), team);
                } else {
                    teamPredicate = cb.equal(root.get("source3AwayTeam"), team);
                }
                break;
            default:
                throw new IllegalArgumentException("无效的数据源参数");
        }

        query.where(cb.and(leaguePredicate, teamPredicate));
        return entityManager.createQuery(query).getResultList();
    }

    private Set<String> findExistingValues(String field, Set<String> values) {
        if (values.isEmpty()) return Collections.emptySet();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Binding> root = query.from(Binding.class);
        query.select(root.get(field)).distinct(true);
        query.where(root.get(field).in(values));

        return new HashSet<>(entityManager.createQuery(query).getResultList());
    }

    private Set<String> findTeamDuplicates(String homeField, String awayField, Set<String> teams) {
        Set<String> result = new HashSet<>();
        if (!teams.isEmpty()) {
            result.addAll(findExistingValues(homeField, teams));
            result.addAll(findExistingValues(awayField, teams));
        }
        return result;
    }

    private void validateSource(int source) {
        if (source < 1 || source > 3) {
            throw new IllegalArgumentException("数据源参数必须为1 - 3");
        }
    }

    private void validateField(String field) {
        if (!VALID_FIELDS.contains(field)) {
            throw new IllegalArgumentException("无效的字段名称: " + field);
        }
    }
}    