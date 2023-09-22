package com.cloudslip.pipeline.service;

import com.cloudslip.pipeline.model.git.AvailableRepos;
import com.cloudslip.pipeline.model.git.Commit;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.util.DateUtil;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GithubService {

    private static final int COMMIT_MSG_LENGTH = 40;

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private Set<String> restrictedApps;

    @Value("${github.token}")
    private String githubToken;

    public String deleteRepo(String appName) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost("api.github.com")
                .setPath("/repos/fagun333/" + appName);

        try {
            restTemplate.exchange(builder.build(), HttpMethod.DELETE, entity, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "exception";
        }

        return "deleted";
    }

    public AvailableRepos getRepos() {
        Map<String, String> params = new HashMap<>();
        params.put("visibility", RepositoryService.TYPE_ALL);
        RepositoryService service = new RepositoryService(gitHubClient);
        try {
            AvailableRepos availableRepos =
                    new AvailableRepos(service.getRepositories(params).stream()
                            .map(Repository::getName)
                            .filter(s -> !restrictedApps.contains(s))
                            .collect(Collectors.toList()));
            return availableRepos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Commit> getCommits(String appName) {
        CommitService commitService = new CommitService(gitHubClient);
        RepositoryId repo = new RepositoryId("fagun333", appName);
        try {
            List<Commit> commits = commitService.getCommits(repo).stream()
                            .map((gitCommit) -> {
                                Commit commit = new Commit();
                                commit.setCommitId(gitCommit.getSha());
                                commit.setCommitMsg(getShortMessage(gitCommit.getCommit().getMessage()));
                                commit.setCommitterId(gitCommit.getCommit().getCommitter().getName());
                                commit.setCommitDate(DateUtil.getFormattedDateFromMili(gitCommit.getCommit().getCommitter().getDate()));
                                return commit;
                            })
                            .collect(Collectors.toList());
            return commits;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDeploymentDescriptor(String repoName, String path, String commitId) {
        ContentsService contentsService = new ContentsService(gitHubClient);
        RepositoryId repo = new RepositoryId("fagun333", repoName);
        try {
            List<RepositoryContents> contents = contentsService.getContents(repo, path, commitId);
            String configYml = new String(Base64.getMimeDecoder().decode(contents.get(0).getContent()));
            return configYml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + githubToken);
        return headers;
    }

    private String getShortMessage(String message) {
        if (message == null || message.length() <= COMMIT_MSG_LENGTH) {
            return message;
        }
        return message.substring(0, COMMIT_MSG_LENGTH) + "...";
    }

}
