package com.cloudslip.pipeline.service;

import com.cloudslip.pipeline.model.Status;
import com.cloudslip.pipeline.model.app.AppDetails;
import com.cloudslip.pipeline.model.app.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AppService {

    @Autowired
    private GithubService githubService;

    @Autowired
    private KubernetesService kubernetesService;

    public AppDetails getAppDetails(String appName) {

        AppDetails appDetails = new AppDetails();
        appDetails.setAppName(appName);
        Environment dev = new Environment();
        dev.setEnvName("DEV");
        dev.setStatus(Status.SUCCESS);
        dev.setCurrentDeployedVersion(kubernetesService.getDeployedGitCommitId(appName));
        dev.setAllTrafficToCurrentVersion(true);
        dev.setAppBaseUrl("http://" + appName + ".sloppytiger.com:9010");
        dev.setGrafanaDashboardUrl("http://grafana.sloppytiger.com:9010/dashboard/db/" + appName + "-metrics-dashboard");
        appDetails.setEnvironments(Arrays.asList(dev));
        appDetails.setCommits(githubService.getCommits(appName));

        return appDetails;
    }


}
