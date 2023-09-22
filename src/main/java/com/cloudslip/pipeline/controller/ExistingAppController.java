package com.cloudslip.pipeline.controller;

import com.cloudslip.pipeline.model.SimpleResponse;
import com.cloudslip.pipeline.model.app.AppDetails;
import com.cloudslip.pipeline.model.git.AvailableRepos;
import com.cloudslip.pipeline.processor.DeploymentProcessor;
import com.cloudslip.pipeline.service.AppService;
import com.cloudslip.pipeline.service.GithubService;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.service.KubernetesService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
public class ExistingAppController {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private GithubService githubService;

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private AppService appService;

    @Autowired
    private Set<String> restrictedApps;

    @Autowired
    private DeploymentProcessor deploymentProcessor;

    @Timed(value = "getAppAccessUrl.time")
    @RequestMapping(value = "/deployedApp/gitCommit/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SimpleResponse> getAppAccessUrl(@PathVariable String appName) throws Exception {
        String gitCommitId = kubernetesService.getDeployedGitCommitId(appName);
        SimpleResponse simpleResponse = new SimpleResponse(gitCommitId);
        return ResponseEntity.ok(simpleResponse);
    }

    @Timed (value = "getRepoNames.time")
    @RequestMapping(value = "/allApps", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<AvailableRepos> getRepoNames() throws Exception {
        AvailableRepos availableRepos = githubService.getRepos();
        return ResponseEntity.ok(availableRepos);
    }

    @Timed (value = "getAppDetails.time")
    @RequestMapping(value = "/app/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<AppDetails> getAppDetails(@PathVariable String appName) throws Exception {
        AppDetails appDetails = appService.getAppDetails(appName);
        return ResponseEntity.ok(appDetails);
    }

    @Timed (value = "deployAppWithVersion.time")
    @RequestMapping(value = "/app/{appName}/deploy/{commitId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SimpleResponse> deployAppWithVersion(@PathVariable String appName, @PathVariable String commitId) throws Exception {
        kubernetesService.deployAppWithChangedVersion(appName, commitId);
        return ResponseEntity.ok(new SimpleResponse("Deployment initiated. commit id: " + commitId));
    }

    @Timed (value = "deleteApp.time")
    @RequestMapping(value = "/delete/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SimpleResponse> deleteApp(@PathVariable String appName) throws Exception {
        if (restrictedApps.contains(appName.toLowerCase())) {
            return ResponseEntity.unprocessableEntity().body(new SimpleResponse("App cannot be deleted !!!"));
        }
        CompletableFuture.supplyAsync(() -> githubService.deleteRepo(appName));
        CompletableFuture.supplyAsync(() -> jenkinsService.deleteAppFromJenkinsAndKube(appName));
        SimpleResponse simpleResponse = new SimpleResponse("App deletion initiated successfully");
        return ResponseEntity.ok(simpleResponse);
    }

    @Timed (value = "getConfig.time")
    @RequestMapping(value = "/config/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SimpleResponse> getDeploymentContent(@PathVariable String appName,
                                                               @RequestParam(required = false) String commitId) throws Exception {
        if (commitId == null) {
            commitId = "master";
        }
        deploymentProcessor.publishDeploymentConfig(appName, "kube-deploy.yml", commitId);
        SimpleResponse simpleResponse = new SimpleResponse("success");
        return ResponseEntity.ok(simpleResponse);
    }
}
