package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.BaseTest;
import com.havluj.github.languageanalyzer.dao.StorageDao;
import com.havluj.github.languageanalyzer.model.LanguageStats;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

class LanguageStatsLogicTest extends BaseTest {

    @MockBean
    private StorageDao storageDaoMock;
    @MockBean
    private LanguageStatsComputeUtil languageStatsComputeUtilMock;

    @Autowired
    private LanguageStatsLogic languageStatsLogic;

    @Test
    void testRefreshStatsInvocations() {
        ArgumentCaptor<SupportedOrg> valueCapture = ArgumentCaptor.forClass(SupportedOrg.class);
        Mockito.when(languageStatsComputeUtilMock.fetchFreshLanguageStatsForOrg(valueCapture.capture()))
                .thenReturn(LANGUAGE_STATS);
        Mockito.doNothing().when(storageDaoMock).updateLanguageStats(Mockito.any(), Mockito.any());

        languageStatsLogic.refreshStats(SupportedOrg.PRODUCTBOARD);

        assertEquals(SupportedOrg.PRODUCTBOARD, valueCapture.getValue());
        Mockito.verify(languageStatsComputeUtilMock, Mockito.times(1))
                .fetchFreshLanguageStatsForOrg(valueCapture.getValue());
        Mockito.verifyNoMoreInteractions(languageStatsComputeUtilMock);
        Mockito.verify(storageDaoMock, Mockito.times(1))
                .updateLanguageStats(valueCapture.getValue().getOrgName(), LANGUAGE_STATS);
        Mockito.verifyNoMoreInteractions(storageDaoMock);
    }

    @Test
    void testGetStatsDbHit() {
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        Mockito.when(storageDaoMock.getLanguageStats(valueCapture.capture()))
                .thenReturn(LANGUAGE_STAT_MAP);

        LanguageStats res = languageStatsLogic.getLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD);

        assertEquals(SupportedOrg.PRODUCTBOARD.getOrgName(), valueCapture.getValue());
        assertEquals(LANGUAGE_STATS, res);

        Mockito.verify(storageDaoMock, Mockito.times(1))
                .getLanguageStats(valueCapture.getValue());
        Mockito.verify(storageDaoMock, Mockito.times(0))
                .updateLanguageStats(valueCapture.getValue(), LANGUAGE_STATS);
        Mockito.verifyNoMoreInteractions(storageDaoMock);
        Mockito.verifyNoInteractions(languageStatsComputeUtilMock);
    }

    @Test
    void testGetStatsDbMiss() {
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        Mockito.when(storageDaoMock.getLanguageStats(valueCapture.capture()))
                .thenReturn(null);
        Mockito.when(languageStatsComputeUtilMock.fetchFreshLanguageStatsForOrg(Mockito.any()))
                .thenReturn(LANGUAGE_STATS);
        Mockito.doNothing().when(storageDaoMock).updateLanguageStats(Mockito.any(), Mockito.any());

        LanguageStats res = languageStatsLogic.getLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD);

        assertEquals(SupportedOrg.PRODUCTBOARD.getOrgName(), valueCapture.getValue());
        assertEquals(LANGUAGE_STATS, res);

        Mockito.verify(storageDaoMock, Mockito.times(1))
                .getLanguageStats(valueCapture.getValue());
        Mockito.verify(languageStatsComputeUtilMock, Mockito.times(1))
                .fetchFreshLanguageStatsForOrg(SupportedOrg.fromName(valueCapture.getValue()));
        Mockito.verify(storageDaoMock, Mockito.times(1))
                .updateLanguageStats(valueCapture.getValue(), LANGUAGE_STATS);
        Mockito.verifyNoMoreInteractions(storageDaoMock);
        Mockito.verifyNoMoreInteractions(languageStatsComputeUtilMock);
    }
}