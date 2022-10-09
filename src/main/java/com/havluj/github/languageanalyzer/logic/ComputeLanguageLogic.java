package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.api.dto.LanguageStatsDto;
import com.havluj.github.languageanalyzer.dao.GitHubDao;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ComputeLanguageLogic {

    // todo make this a param in the api
    private static final int DECIMAL_PRECISION = 2;

    @Autowired
    GitHubDao gitHubDao;

    public LanguageStatsDto computeLanguageStatsForOrg(@NonNull final String orgName) {
        log.debug("Computing sums for all languages across all repos.");
        final List<GHRepository> repos = gitHubDao.listOrgRepos(gitHubDao.getOrg(orgName));
        final Map<String, BigDecimal> langStats = getLanguageSums(repos);
        return new LanguageStatsDto(langStats);
    }

    private Map<String, BigDecimal> getLanguageSums(@NonNull final List<GHRepository> repos) {
        log.debug("Computing sums for all languages across all repos.");
        BigDecimal totalSum = new BigDecimal(0);
        Map<String, BigDecimal> languageSums = new HashMap<>();
        for (GHRepository repo : repos) {
            for (Map.Entry<String, Long> lang : gitHubDao.listRepoLanguages(repo).entrySet()) {
                final BigDecimal byteCnt = new BigDecimal(lang.getValue());
                languageSums.compute(lang.getKey(), (k, v) -> v == null ? byteCnt : v.add(byteCnt));
                totalSum = totalSum.add(byteCnt);
            }
        }
        log.debug(String.format("Finished computing sums for all languages across all repos. Total bytes: [%s]", totalSum));
        if (totalSum.compareTo(new BigDecimal(0)) > 0) {
            return computePercentages(totalSum, languageSums);
        } else {
            log.debug("All repositories are empty.");
            return new HashMap<>();
        }
    }

    private Map<String, BigDecimal> computePercentages(@NonNull final BigDecimal total,
                                                       @NonNull final Map<String, BigDecimal> components) {
        log.debug("Computing percentages for languages used.");
        Map<String, BigDecimal> languageSums = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : components.entrySet()) {
            final String language = entry.getKey();
            final BigDecimal percentage = entry.getValue().divide(total, DECIMAL_PRECISION, RoundingMode.HALF_DOWN);
            languageSums.put(language, percentage);
            log.debug(String.format("[%s]: %s.", language, percentage));
        }
        log.debug("Finished computing percentages for languages used.");
        return languageSums;
    }
}
