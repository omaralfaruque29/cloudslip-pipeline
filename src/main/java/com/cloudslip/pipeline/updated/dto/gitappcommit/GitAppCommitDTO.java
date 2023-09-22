package com.cloudslip.pipeline.updated.dto.gitappcommit;

public class GitAppCommitDTO {
    private String sha;
    private GitCommitDTO commit;

    public GitAppCommitDTO() {
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public GitCommitDTO getCommit() {
        return commit;
    }

    public void setCommit(GitCommitDTO commit) {
        this.commit = commit;
    }
}
