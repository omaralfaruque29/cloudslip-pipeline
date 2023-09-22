package com.cloudslip.pipeline.model.git;

public class Commit {

    private String commitId;
    private String commitMsg;
    private String commitDate;
    private String committerId;

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(final String commitId) {
        this.commitId = commitId;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    public void setCommitMsg(final String commitMsg) {
        this.commitMsg = commitMsg;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(final String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitterId() {
        return committerId;
    }

    public void setCommitterId(final String committerId) {
        this.committerId = committerId;
    }
}
