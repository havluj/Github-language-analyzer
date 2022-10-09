package com.havluj.github.languageanalyzer.logic;

import com.havluj.github.languageanalyzer.BaseTest;
import com.havluj.github.languageanalyzer.dao.GitHubDao;
import com.havluj.github.languageanalyzer.exceptions.GitHubIoErrorException;
import com.havluj.github.languageanalyzer.exceptions.OrgNotFoundException;
import com.havluj.github.languageanalyzer.model.LanguageStats;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LanguageStatsComputeUtilTest extends BaseTest {

    @MockBean
    private GitHubDao gitHubDaoMock;

    @Mock private GHRepository repository1Mock;
    @Mock private GHRepository repository2Mock;
    @Mock private GHOrganization organizationMock;

    @Autowired
    LanguageStatsComputeUtil languageStatsComputeUtil;

    @Test
    void testOrgNotFound() {
        Mockito.when(gitHubDaoMock.getOrg(Mockito.any()))
                .thenThrow(OrgNotFoundException.class);

        assertThrows(OrgNotFoundException.class,
                () -> languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD));
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .getOrg(SupportedOrg.PRODUCTBOARD.getOrgName());
        Mockito.verifyNoMoreInteractions(gitHubDaoMock);
    }

    @Test
    void testIoExceptionWhenGettingRepositories() {
        Mockito.when(gitHubDaoMock.getOrg(Mockito.any()))
                .thenReturn(organizationMock);
        Mockito.when(gitHubDaoMock.listOrgRepos(organizationMock))
                .thenThrow(GitHubIoErrorException.class);

        assertThrows(GitHubIoErrorException.class,
                () -> languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD));
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .getOrg(SupportedOrg.PRODUCTBOARD.getOrgName());
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listOrgRepos(organizationMock);
        Mockito.verifyNoMoreInteractions(gitHubDaoMock);
    }

    @Test
    void testNoRepositories() {
        Mockito.when(gitHubDaoMock.getOrg(Mockito.any()))
                .thenReturn(organizationMock);
        Mockito.when(gitHubDaoMock.listOrgRepos(organizationMock))
                .thenReturn(new ArrayList<>());

        LanguageStats res = languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD);

        assertEquals(EMPTY_LANGUAGE_STATS, res);

        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .getOrg(SupportedOrg.PRODUCTBOARD.getOrgName());
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listOrgRepos(organizationMock);
        Mockito.verifyNoMoreInteractions(gitHubDaoMock);
    }

    @Test
    void testRepoReadFailure() {
        Mockito.when(gitHubDaoMock.getOrg(Mockito.any()))
                .thenReturn(organizationMock);
        Mockito.when(gitHubDaoMock.listOrgRepos(organizationMock))
                .thenReturn(List.of(repository1Mock));
        Mockito.when(gitHubDaoMock.listRepoLanguages(repository1Mock))
                .thenThrow(GitHubIoErrorException.class);

        assertThrows(GitHubIoErrorException.class,
                () -> languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD));

        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .getOrg(SupportedOrg.PRODUCTBOARD.getOrgName());
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listOrgRepos(organizationMock);
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listRepoLanguages(repository1Mock);
        Mockito.verifyNoMoreInteractions(gitHubDaoMock);
    }

    @Test
    void testHappyCase() {
        Mockito.when(gitHubDaoMock.getOrg(Mockito.any()))
                .thenReturn(organizationMock);
        Mockito.when(gitHubDaoMock.listOrgRepos(organizationMock))
                .thenReturn(List.of(repository1Mock, repository2Mock));
        Mockito.when(gitHubDaoMock.listRepoLanguages(repository1Mock))
                .thenReturn(REPO_STAT_1);
        Mockito.when(gitHubDaoMock.listRepoLanguages(repository2Mock))
                .thenReturn(REPO_STAT_2);

        LanguageStats res = languageStatsComputeUtil.fetchFreshLanguageStatsForOrg(SupportedOrg.PRODUCTBOARD);

        assertEquals(LANGUAGE_STATS, res);

        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .getOrg(SupportedOrg.PRODUCTBOARD.getOrgName());
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listOrgRepos(organizationMock);
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listRepoLanguages(repository1Mock);
        Mockito.verify(gitHubDaoMock, Mockito.times(1))
                .listRepoLanguages(repository2Mock);
        Mockito.verifyNoMoreInteractions(gitHubDaoMock);
    }
}