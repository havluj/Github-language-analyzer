package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.BaseTest;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

class StatRefresherTest extends BaseTest {

    @MockBean
    private LanguageStatsLogic statsLogicMock;

    private StatRefresher statRefresher;

    @BeforeEach
    void setUp() {
        statRefresher = new StatRefresher(statsLogicMock);
    }

    @Test
    void testStartupInvocations() {
        Mockito.when(statsLogicMock.getLanguageStatsForOrg(Mockito.any()))
                .thenReturn(LANGUAGE_STATS);

        statRefresher.updateDataOnStartup();

        for (SupportedOrg org : SupportedOrg.values()) {
            Mockito.verify(statsLogicMock, Mockito.times(1)).getLanguageStatsForOrg(org);
        }
    }

    @Test
    void testForceUpdateInvocations() {
        Mockito.doNothing().when(statsLogicMock).refreshStats(Mockito.any());

        statRefresher.updateData();

        for (SupportedOrg org : SupportedOrg.values()) {
            Mockito.verify(statsLogicMock, Mockito.times(1)).refreshStats(org);
        }
    }
}