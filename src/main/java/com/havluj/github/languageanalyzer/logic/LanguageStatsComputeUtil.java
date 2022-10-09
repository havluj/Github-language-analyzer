package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.dao.GitHubDao;
import com.havluj.github.languageanalyzer.model.LanguageStats;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
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
public class LanguageStatsComputeUtil {

    @Autowired
    private GitHubDao gitHubDao;

    public LanguageStats fetchFreshLanguageStatsForOrg(@NonNull final SupportedOrg org) {
        log.debug("Computing sums for all languages across all repos.");
        final List<GHRepository> repos = gitHubDao.listOrgRepos(gitHubDao.getOrg(org.getOrgName()));

        final Map<String, String> langStats = getLanguageSums(repos);
        return new LanguageStats(langStats);
    }

    private Map<String, String> getLanguageSums(@NonNull final List<GHRepository> repos) {
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

    private Map<String, String> computePercentages(@NonNull final BigDecimal total,
                                                   @NonNull final Map<String, BigDecimal> components) {
        log.debug("Computing percentages for languages used.");
        Map<String, String> languageSums = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : components.entrySet()) {
            final String language = entry.getKey();
            final String serializedPercentage = serialize(getPercentage(total, entry.getValue()));
            languageSums.put(language, serializedPercentage);
            log.debug(String.format("[%s]: %s.", language, serializedPercentage));
        }
        log.debug("Finished computing percentages for languages used.");
        return languageSums;
    }

    private BigDecimal getPercentage(@NonNull final BigDecimal denominator, @NonNull final BigDecimal numerator) {
        return numerator.divide(denominator, 2, RoundingMode.HALF_EVEN);
    }

    private String serialize(@NonNull final BigDecimal num) {
        return String.format("%,.2f", num.setScale(2, RoundingMode.HALF_EVEN));
    }
}
