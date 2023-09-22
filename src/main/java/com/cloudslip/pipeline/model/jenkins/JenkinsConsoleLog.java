package com.cloudslip.pipeline.model.jenkins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties (ignoreUnknown = true)
public class JenkinsConsoleLog {

    private Integer textSize;
    private Boolean hasMoreText;
    private String nextLogUrl;
    private String logText;

    public Integer getTextSize() {
        return textSize;
    }

    public void setTextSize(final Integer textSize) {
        this.textSize = textSize;
    }

    public Boolean isHasMoreText() {
        return hasMoreText;
    }

    public void setHasMoreText(final Boolean hasMoreText) {
        this.hasMoreText = hasMoreText;
    }

    public String getNextLogUrl() {
        return nextLogUrl;
    }

    public void setNextLogUrl(final String nextLogUrl) {
        this.nextLogUrl = nextLogUrl;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(final String logText) {
        this.logText = logText;
    }
}
