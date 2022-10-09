package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.model.SupportedOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatRefresher {

    @Autowired LanguageStatsLogic statsLogic;

    /**
     * Application warmup.
     *
     * Gets language stats for every supported org upon startup. That will load stats from disk to memory, or if the
     * stats are not on disk, it will fetch fresh stats from GitHub and store them. Because of this, we won't have to
     * fetch/compute stats during the first request.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void updateDataOnStartup() {
        for (SupportedOrg org : SupportedOrg.values()) {
            statsLogic.getLanguageStatsForOrg(org);
        }
    }

    /**
     * Fetches new stats from GitHub for all supported orgs. Runs every day at 2:30 PM.
     * Cron syntax: second, minute, hour, day of month, month, day(s) of week
     *
     * By default, Spring uses a local single-threaded scheduler to run the task, which is acceptable for this use-case.
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void updateData() {
        for (SupportedOrg org : SupportedOrg.values()) {
            statsLogic.refreshStats(org);
        }
    }
}
