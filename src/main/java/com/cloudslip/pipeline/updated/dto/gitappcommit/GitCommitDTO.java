package com.cloudslip.pipeline.updated.dto.gitappcommit;

public class GitCommitDTO {
    private GitCommitCommitterDTO committer;
    private String message;

    public GitCommitCommitterDTO getCommitter() {
        return committer;
    }

    public void setCommitter(GitCommitCommitterDTO committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GitCommitDTO() {
    }
}
