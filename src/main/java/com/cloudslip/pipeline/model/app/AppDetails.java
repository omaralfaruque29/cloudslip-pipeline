package com.cloudslip.pipeline.model.app;

import com.cloudslip.pipeline.model.git.Commit;

import java.util.List;

public class AppDetails {

    private String appName;
    private List<Environment> environments;
    private List<Commit> commits;

    public String getAppName() {
        return appName;
    }

    public void setAppName(final String appName) {
        this.appName = appName;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(final List<Environment> environments) {
        this.environments = environments;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(final List<Commit> commits) {
        this.commits = commits;
    }
}
