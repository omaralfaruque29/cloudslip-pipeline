package com.cloudslip.pipeline.model.git;

import java.util.List;

public class AvailableRepos {

    private List<String> repos;

    public AvailableRepos(final List<String> repos) {
        this.repos = repos;
    }

    public List<String> getRepos() {
        return repos;
    }
}
