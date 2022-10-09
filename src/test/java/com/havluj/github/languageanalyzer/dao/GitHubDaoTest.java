package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.BaseTest;
import com.havluj.github.languageanalyzer.exceptions.GitHubIoErrorException;
import com.havluj.github.languageanalyzer.exceptions.OrgNotFoundException;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GitHubDaoTest extends BaseTest {

    @Mock private GitHub gitHubClientMock;
    @Mock private GHRepository repository1Mock;
    @Mock private GHRepository repository2Mock;
    @Mock private GHOrganization organizationMock;

    private GitHubDao gitHubDao;

    @BeforeEach
    void setUp() throws IOException {
        gitHubDao = GitHubDao.withClient(gitHubClientMock);
    }

    @Test
    void testOrgNotFound() throws IOException {
        Mockito.when(gitHubClientMock.getOrganization(Mockito.any()))
                .thenThrow(IOException.class);

        assertThrows(OrgNotFoundException.class, () -> gitHubDao.getOrg(SupportedOrg.PRODUCTBOARD.getOrgName()));

        Mockito.verify(gitHubClientMock, Mockito.times(1)).getOrganization(Mockito.any());
        Mockito.verifyNoMoreInteractions(gitHubClientMock);
    }

    @Test
    void testGetOrg() throws IOException {
        Mockito.when(gitHubClientMock.getOrganization(Mockito.any()))
                .thenReturn(organizationMock);

        assertEquals(organizationMock, gitHubDao.getOrg(SupportedOrg.PRODUCTBOARD.getOrgName()));

        Mockito.verify(gitHubClientMock, Mockito.times(1)).getOrganization(Mockito.any());
        Mockito.verifyNoMoreInteractions(gitHubClientMock);
    }

    @Test
    void testIoExceptionWhenGettingRepositories() throws IOException {
        Mockito.when(organizationMock.getRepositories()).thenThrow(IOException.class);

        assertThrows(GitHubIoErrorException.class, () -> gitHubDao.listOrgRepos(organizationMock));

        Mockito.verify(organizationMock, Mockito.times(1)).getRepositories();
        Mockito.verifyNoMoreInteractions(organizationMock);
        Mockito.verifyNoInteractions(gitHubClientMock);
    }

    @Test
    void testGetRepositories() throws IOException {
        final Map<String, GHRepository> org_repos = Map.of("r1", repository1Mock, "r2", repository2Mock);
        Mockito.when(organizationMock.getRepositories()).thenReturn(org_repos);

        final List<GHRepository> res = gitHubDao.listOrgRepos(organizationMock);
        assertEquals(2, res.size());
        assertTrue(res.containsAll(List.of(repository1Mock, repository2Mock)));

        Mockito.verify(organizationMock, Mockito.times(1)).getRepositories();
        Mockito.verifyNoMoreInteractions(organizationMock);
        Mockito.verifyNoInteractions(gitHubClientMock);
    }

    @Test
    void testIoExceptionWhenGettingRepoLangStats() throws IOException {
        Mockito.when(repository1Mock.listLanguages()).thenThrow(IOException.class);

        assertThrows(GitHubIoErrorException.class, () -> gitHubDao.listRepoLanguages(repository1Mock));

        Mockito.verify(repository1Mock, Mockito.times(1)).listLanguages();
        Mockito.verifyNoMoreInteractions(repository1Mock);
        Mockito.verifyNoInteractions(organizationMock);
        Mockito.verifyNoInteractions(gitHubClientMock);
    }

    @Test
    void testGetRepoLanguages() throws IOException {
        final Map<String, GHRepository> org_repos = Map.of("r1", repository1Mock, "r2", repository2Mock);
        Mockito.when(repository1Mock.listLanguages())
                .thenReturn(REPO_STAT_1);

        assertEquals(REPO_STAT_1, gitHubDao.listRepoLanguages(repository1Mock));

        Mockito.verify(repository1Mock, Mockito.times(1)).listLanguages();
        Mockito.verifyNoMoreInteractions(repository1Mock);
        Mockito.verifyNoInteractions(organizationMock);
        Mockito.verifyNoInteractions(gitHubClientMock);
    }
}