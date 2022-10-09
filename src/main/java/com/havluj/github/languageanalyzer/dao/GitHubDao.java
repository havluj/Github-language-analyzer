package com.havluj.github.languageanalyzer.dao;

import com.havluj.github.languageanalyzer.exceptions.GitHubIoErrorException;
import com.havluj.github.languageanalyzer.exceptions.OrgNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GitHubDao {

    private final GitHub client;

    /**
     * A wrapper for a Java client: https://github-api.kohsuke.org/. The library lazy-loads a lot of information.
     * Because of that, don't access information that you don't need until you truly need it -- for example, once you
     * try to access an organization's name, the library will make the network call to get the org's metadata.
     * Therefore, if you only need to build certain objects to get to their properties (e.g. to get org's repos),
     * don't access the org's other metadata unless you truly need it.
     *
     * @throws IOException if a connection to GitHub fails.
     */
    public GitHubDao(@Value("${github.token}") final String token) throws IOException {
        this.client = (new GitHubBuilder()).withOAuthToken(token).build();
    }

    public GHOrganization getOrg(@NonNull String orgName) {
        try {
            log.debug(String.format("Getting org info from GitHub. Org name: [%s].", orgName));
            return client.getOrganization(orgName);
        } catch (IOException e) {
            log.error(String.format("Failed getting org info from GitHub. Org name: [%s].", orgName), e);
            throw new OrgNotFoundException(e);
        }
    }

    /**
     * Obtains a list of all repositories we have access to. That means all public repositories and those private
     * repositories that we are authorized to see.
     * API description: https://docs.github.com/en/rest/repos/repos#list-organization-repositories
     *
     * @return A list of all repositories that we have access to.
     */
    public List<GHRepository> listOrgRepos(@NonNull GHOrganization organization) {
        try {
            log.debug("Getting a list of repos.");
            return new ArrayList<>(organization.getRepositories().values());
        } catch (IOException e) {
            log.error("Failed getting a list of repos from GitHub.", e);
            throw new GitHubIoErrorException(e);
        }
    }

    /**
     * Obtains a map of languages for the specified repository. The value shown for each language is the number
     * of bytes of code written in that language.
     * API description: https://docs.github.com/en/rest/repos/repos#list-repository-languages
     *
     * @param repository GHRepository we want to know about.
     * @return A
     */
    public Map<String, Long> listRepoLanguages(@NonNull GHRepository repository) {
        try {
            log.debug("Getting a list of languages for a repo.");
            return repository.listLanguages();
        } catch (IOException e) {
            log.error("Failed getting a list of repos from GitHub.", e);
            throw new GitHubIoErrorException(e);
        }
    }
}
