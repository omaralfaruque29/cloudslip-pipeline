package com.cloudslip.pipeline.config;

import com.offbytwo.jenkins.JenkinsServer;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.auth.ApiKeyAuth;
import io.kubernetes.client.util.Config;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public JenkinsServer jenkinsServer(@Value("${jenkins.url}") String jenkinsUrl,
                                       @Value("${jenkins.token}") String jenkinsToken,
                                       @Value("${jenkins.user}") String jenkinsUser) throws URISyntaxException {
        return new JenkinsServer(new URI(jenkinsUrl), jenkinsUser, jenkinsToken);
    }

    @Bean
    public Set<String> restrictedApps() {
        Set<String> restrictedApps = new HashSet<>();
        restrictedApps.add("cloudslip-pipeline");
        restrictedApps.add("nodeapp");
        restrictedApps.add("prometheus");
        restrictedApps.add("mongo");
        restrictedApps.add("kubernetes");
        restrictedApps.add("create-app");
        restrictedApps.add("delete-app-deployment");
        return restrictedApps;
    }

    /*@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }*/

    @Bean
    public ApiClient apiClient(@Value("${kubernetes.url}") String kubeUrl, @Value("${kubernetes.auth.token}") String token) {
        ApiClient apiClient = Config.fromUrl(kubeUrl,false);
        ApiKeyAuth bearerToken = (ApiKeyAuth) apiClient.getAuthentication("BearerToken");
        bearerToken.setApiKey(token);
        bearerToken.setApiKeyPrefix("Bearer");
        io.kubernetes.client.Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    @Bean
    public GitHubClient gitHubClient(@Value("${github.token}") String githubToken) {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(githubToken);
        return client;
    }
}
