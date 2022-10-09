package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.dao.StorageDao;
import com.havluj.github.languageanalyzer.model.LanguageStats;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class LanguageStatsLogic {

    @Autowired
    private StorageDao storageDao;
    @Autowired
    private LanguageStatsComputeUtil languageStatsComputeUtil;

    /**
     * Gets language stats for a given org. It will try to fetch data from the storage DAO. If no stats are returned,
     * it will try to fetch, compute, and store fresh data from GitHub.
     */
    public LanguageStats getLanguageStatsForOrg(@NonNull final SupportedOrg org) {
        log.debug(String.format("Trying to fetch languages stats for org [%s].", org));
        Map<String, String> stats = storageDao.getLanguageStats(org.getOrgName());

        if (stats == null) {
            // this means we don't have the data in memory or on disk
            log.debug("No stats found, will refresh.");
            return fetchAndStore(org);
        } else {
            log.debug("Valid stats found, returning.");
            return new LanguageStats(stats);
        }
    }

    /**
     * Downloads fresh data from GitHub and uses it to compute new language stats. Those stats then get saved (as a new
     * value or overriding an existing value).
     */
    public void refreshStats(@NonNull final SupportedOrg org) {
        log.debug(String.format("Starting refresh of stats for org: %s.", org));
        fetchAndStore(org);
    }

    private LanguageStats fetchAndStore(@NonNull final SupportedOrg org) {
        final LanguageStats ls = languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(org);
        log.debug(String.format("%s's stats computed. Persisting.", org));
        storageDao.updateLanguageStats(org.getOrgName(), ls);
        return ls;
    }

}
