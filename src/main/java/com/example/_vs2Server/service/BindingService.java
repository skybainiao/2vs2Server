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
            String league1 = request.getSource1().getLeagueName();
            binding.setSource1League(league1);
            binding.setSource1HomeTeam(checkAndSetNull(league1, request.getSource1().getHomeTeam()));
            binding.setSource1AwayTeam(checkAndSetNull(league1, request.getSource1().getAwayTeam()));
            // Source 2
            String league2 = request.getSource2().getLeagueName();
            binding.setSource2League(league2);
            binding.setSource2HomeTeam(checkAndSetNull(league2, request.getSource2().getHomeTeam()));
            binding.setSource2AwayTeam(checkAndSetNull(league2, request.getSource2().getAwayTeam()));
            // Source 3
            String league3 = request.getSource3().getLeagueName();
            binding.setSource3League(league3);
            binding.setSource3HomeTeam(checkAndSetNull(league3, request.getSource3().getHomeTeam()));
            binding.setSource3AwayTeam(checkAndSetNull(league3, request.getSource3().getAwayTeam()));
            bindingRepository.save(binding);
        });
    }

    private String checkAndSetNull(String league, String value) {
        if (value == null) {
            return null;
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Binding> query = cb.createQuery(Binding.class);
        Root<Binding> root = query.from(Binding.class);

        Predicate leaguePredicate1 = cb.equal(root.get("source1League"), league);
        Predicate leaguePredicate2 = cb.equal(root.get("source2League"), league);
        Predicate leaguePredicate3 = cb.equal(root.get("source3League"), league);

        Predicate teamPredicate1 = cb.equal(root.get("source1HomeTeam"), value);
        Predicate teamPredicate2 = cb.equal(root.get("source1AwayTeam"), value);
        Predicate teamPredicate3 = cb.equal(root.get("source2HomeTeam"), value);
        Predicate teamPredicate4 = cb.equal(root.get("source2AwayTeam"), value);
        Predicate teamPredicate5 = cb.equal(root.get("source3HomeTeam"), value);
        Predicate teamPredicate6 = cb.equal(root.get("source3AwayTeam"), value);

        Predicate finalPredicate = cb.and(
                cb.or(leaguePredicate1, leaguePredicate2, leaguePredicate3),
                cb.or(teamPredicate1, teamPredicate2, teamPredicate3, teamPredicate4, teamPredicate5, teamPredicate6)
        );

        query.where(finalPredicate);
        List<Binding> result = entityManager.createQuery(query).getResultList();

        return result.isEmpty() ? value : null;
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

        Map<String, Set<String>> leagueTeamsMap = new HashMap<>();
        request.getMatches().forEach(match -> {
            String league = match.getLeague();
            leagueTeamsMap.computeIfAbsent(league, k -> new HashSet<>());
            leagueTeamsMap.get(league).add(match.getHomeTeam());
            leagueTeamsMap.get(league).add(match.getAwayTeam());
        });

        Set<String> allLeagues = new HashSet<>();
        Set<String> allTeams = new HashSet<>();

        leagueTeamsMap.forEach((league, teams) -> {
            // ✅ 仅查询当前数据源对应的联赛字段（如source1League=league）
            allLeagues.addAll(findExistingValuesInLeague(
                    league,
                    leagueField,  // 例如 "source1League"（与数据源强绑定）
                    leagueField,  // 目标字段是联赛字段本身（用于标记重复联赛）
                    Collections.singleton(league)
            ));
            // ✅ 仅查询当前数据源对应的球队字段（如source1HomeTeam/source1AwayTeam）
            allTeams.addAll(findTeamDuplicatesInLeague(
                    league,
                    leagueField,  // 数据源对应的联赛字段（如source1League）
                    homeField,    // 数据源对应的主队字段（如source1HomeTeam）
                    awayField,    // 数据源对应的客队字段（如source1AwayTeam）
                    teams
            ));
        });

        return Map.of(
                "leagues", new ArrayList<>(allLeagues),
                "teams", new ArrayList<>(allTeams)
        );
    }

    private Set<String> findTeamDuplicatesInLeague(
            String league,
            String leagueField,  // 当前数据源的联赛字段（如source1League）
            String homeField,    // 当前数据源的主队字段（如source1HomeTeam）
            String awayField,    // 当前数据源的客队字段（如source1AwayTeam）
            Set<String> teams
    ) {
        Set<String> result = new HashSet<>();
        if (!teams.isEmpty()) {
            // ✅ 仅查询当前数据源的主队字段是否在联赛+球队条件下重复
            result.addAll(findExistingValuesInLeague(league, leagueField, homeField, teams));
            // ✅ 仅查询当前数据源的客队字段是否在联赛+球队条件下重复
            result.addAll(findExistingValuesInLeague(league, leagueField, awayField, teams));
        }
        return result;
    }

    private Set<String> findExistingValuesInLeague(
            String league,
            String leagueField,  // 当前数据源的联赛字段（如source1League）
            String targetField,  // 目标字段（可能是联赛字段或球队字段）
            Set<String> values
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Binding> root = query.from(Binding.class);

        // ✅ 新增条件：联赛字段必须等于当前联赛（且属于当前数据源）
        Predicate leaguePredicate = cb.equal(root.get(leagueField), league);
        // 目标字段（如主队/客队）在传入的球队集合中
        Predicate valuePredicate = root.get(targetField).in(values);

        query.select(root.get(targetField))
                .where(cb.and(leaguePredicate, valuePredicate)) // 必须同时满足数据源联赛+目标值
                .distinct(true);

        return new HashSet<>(entityManager.createQuery(query).getResultList());
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