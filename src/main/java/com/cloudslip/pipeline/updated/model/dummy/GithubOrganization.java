package com.cloudslip.pipeline.updated.model.dummy;

import java.io.Serializable;

public class GithubOrganization implements Serializable {
    private long id;
    private String login;

    public GithubOrganization() {
    }

    public GithubOrganization(long id, String login) {
        this.id = id;
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
