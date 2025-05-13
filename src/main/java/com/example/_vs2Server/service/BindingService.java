package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.dto.CheckDuplicateRequest;
import com.example._vs2Server.dto.SourceData;
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
            // 提取三个数据源的联赛名称（与数据源编号1/2/3一一对应）
            String league1 = request.getSource1().getLeagueName();
            String league2 = request.getSource2().getLeagueName();
            String league3 = request.getSource3().getLeagueName();

            Binding binding = new Binding();

            // 处理数据源1：主队和客队，数据源编号为1
            SourceData source1Data = request.getSource1();
            binding.setSource1League(league1);
            binding.setSource1HomeTeam(checkAndSetNull(1, source1Data.getHomeTeam(), league1, league2, league3));
            binding.setSource1AwayTeam(checkAndSetNull(1, source1Data.getAwayTeam(), league1, league2, league3));

            // 处理数据源2：主队和客队，数据源编号为2
            SourceData source2Data = request.getSource2();
            binding.setSource2League(league2);
            binding.setSource2HomeTeam(checkAndSetNull(2, source2Data.getHomeTeam(), league1, league2, league3));
            binding.setSource2AwayTeam(checkAndSetNull(2, source2Data.getAwayTeam(), league1, league2, league3));

            // 处理数据源3：主队和客队，数据源编号为3（关键修复：此处编号为3）
            SourceData source3Data = request.getSource3();
            binding.setSource3League(league3);
            binding.setSource3HomeTeam(checkAndSetNull(3, source3Data.getHomeTeam(), league1, league2, league3)); // 数据源编号3
            binding.setSource3AwayTeam(checkAndSetNull(3, source3Data.getAwayTeam(), league1, league2, league3)); // 数据源编号3

            bindingRepository.save(binding);
        });
    }

    private String checkAndSetNull(int dataSource, String value, String league1, String league2, String league3) {
        if (value == null) {
            return null;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Binding> query = cb.createQuery(Binding.class);
        Root<Binding> root = query.from(Binding.class);

        // 联赛组合条件：必须同时匹配三个数据源的联赛（与数据源编号无关，仅校验联赛组合）
        Predicate leagueCombinationPredicate = cb.and(
                cb.equal(root.get("source1League"), league1),
                cb.equal(root.get("source2League"), league2),
                cb.equal(root.get("source3League"), league3)
        );

        // 根据数据源编号（1/2/3）确定字段前缀，彻底解耦联赛名称与数据源的关系
        String sourcePrefix;
        switch (dataSource) {
            case 1:
                sourcePrefix = "source1";
                break;
            case 2:
                sourcePrefix = "source2";
                break;
            case 3:
                sourcePrefix = "source3";
                break;
            default:
                throw new IllegalArgumentException("无效的数据源编号，必须为1、2或3");
        }

        // 球队存在性条件：当前数据源的主队或客队字段等于目标值
        Predicate teamPredicate = cb.or(
                cb.equal(root.get(sourcePrefix + "HomeTeam"), value),
                cb.equal(root.get(sourcePrefix + "AwayTeam"), value)
        );

        // 组合查询条件：联赛组合匹配 + 当前数据源球队匹配
        query.where(cb.and(leagueCombinationPredicate, teamPredicate));
        List<Binding> result = entityManager.createQuery(query).getResultList();

        // 若存在重复记录，返回null（置空字段），否则保留原值
        return result.isEmpty() ? value : null;
    }

    public List<Binding> getAllBindings() {
        return bindingRepository.findAll();
    }

    public Map<String, Object> checkDuplicates(CheckDuplicateRequest request) {
        int source = request.getSource();
        validateSource(source);

        // 动态获取当前数据源的字段名
        String currentLeagueField = "source" + source + "League";
        String currentHomeField = "source" + source + "HomeTeam";
        String currentAwayField = "source" + source + "AwayTeam";

        validateField(currentLeagueField);
        validateField(currentHomeField);
        validateField(currentAwayField);

        // 存储最终结果
        Set<String> duplicateLeagues = new HashSet<>();
        Set<String> duplicateTeams = new HashSet<>();

        // 按联赛分组处理
        Map<String, Set<String>> leagueTeamsMap = new HashMap<>();
        request.getMatches().forEach(match -> {
            String league = match.getLeague();
            leagueTeamsMap.computeIfAbsent(league, k -> new HashSet<>());
            leagueTeamsMap.get(league).add(match.getHomeTeam());
            leagueTeamsMap.get(league).add(match.getAwayTeam());
        });

        // 处理每个联赛
        leagueTeamsMap.forEach((league, teams) -> {
            // 1. 检查联赛是否已存在（仅在当前数据源）
            Set<String> existingLeagues = findExistingValuesInLeague(
                    league,
                    currentLeagueField,  // 使用当前数据源的联赛字段
                    currentLeagueField,  // 目标字段也是当前数据源的联赛字段
                    Collections.singleton(league)
            );
            if (!existingLeagues.isEmpty()) {
                duplicateLeagues.addAll(existingLeagues);
            }

            // 2. 检查球队是否重复（仅在当前联赛内）
            if (!teams.isEmpty()) {
                // 只检查当前数据源的主客队字段
                Set<String> existingHomeTeams = findExistingValuesInLeague(
                        league,
                        currentLeagueField,
                        currentHomeField,
                        teams
                );
                Set<String> existingAwayTeams = findExistingValuesInLeague(
                        league,
                        currentLeagueField,
                        currentAwayField,
                        teams
                );

                duplicateTeams.addAll(existingHomeTeams);
                duplicateTeams.addAll(existingAwayTeams);
            }
        });

        return Map.of(
                "leagues", new ArrayList<>(duplicateLeagues),
                "teams", new ArrayList<>(duplicateTeams)
        );
    }



    private Set<String> findExistingValuesInLeague(
            String league,
            String leagueField,  // 当前数据源的联赛字段
            String targetField,  // 当前数据源的目标字段
            Set<String> values) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Binding> root = query.from(Binding.class);

        // 关键修改点：只检查当前数据源的联赛字段
        Predicate leaguePredicate = cb.equal(root.get(leagueField), league);
        Predicate valuePredicate = root.get(targetField).in(values);

        query.select(root.get(targetField))
                .where(cb.and(leaguePredicate, valuePredicate))
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


    // 新增方法：检查比赛是否存在于数据库
    public Map<String, Object> checkMatchesExisting(CheckDuplicateRequest request) {
        int source = request.getSource();
        validateSource(source);

        // 动态获取当前数据源的字段名
        String currentLeagueField = "source" + source + "League";
        String currentHomeField = "source" + source + "HomeTeam";
        String currentAwayField = "source" + source + "AwayTeam";

        validateField(currentLeagueField);
        validateField(currentHomeField);
        validateField(currentAwayField);

        // 提取所有联赛和球队
        Set<String> allLeagues = new HashSet<>();
        Set<String> allTeams = new HashSet<>();

        for (CheckDuplicateRequest.MatchData match : request.getMatches()) {
            allLeagues.add(match.getLeague());
            allTeams.add(match.getHomeTeam());
            allTeams.add(match.getAwayTeam());
        }

        // 查询数据库中存在的联赛和球队
        Set<String> existingLeagues = findExistingValuesInAnyLeague(
                currentLeagueField,
                allLeagues
        );

        Set<String> existingTeams = findExistingValuesInAnyLeague(
                currentHomeField,
                allTeams
        );

        existingTeams.addAll(findExistingValuesInAnyLeague(
                currentAwayField,
                allTeams
        ));

        return Map.of(
                "leagues", existingLeagues,
                "teams", existingTeams
        );
    }


    // 新增辅助方法：在任意联赛中查找存在的值
    private Set<String> findExistingValuesInAnyLeague(
            String targetField,
            Set<String> values) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Binding> root = query.from(Binding.class);

        Predicate valuePredicate = root.get(targetField).in(values);

        query.select(root.get(targetField))
                .where(valuePredicate)
                .distinct(true);

        return new HashSet<>(entityManager.createQuery(query).getResultList());
    }
}