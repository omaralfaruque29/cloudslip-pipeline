package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.cloudslip.pipeline.updated.model.dummy.GitDirectory;

public class Organization extends BaseEntity {

    private String name;
    private String description;
    private Company company;
    private GitDirectory gitDirectory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public GitDirectory getGitDirectory() {
        return gitDirectory;
    }

    public void setGitDirectory(GitDirectory gitDirectory) {
        this.gitDirectory = gitDirectory;
    }
}
