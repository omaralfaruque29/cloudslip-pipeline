package com.cloudslip.pipeline.updated.model.dummy;


import java.io.Serializable;

public class GitDirectory implements Serializable {
    private long id;
    private String name;

    public GitDirectory() {
    }

    public GitDirectory(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public GitDirectory(GithubOrganization githubOrganization) {
        this.id = githubOrganization.getId();
        this.name = githubOrganization.getLogin();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
