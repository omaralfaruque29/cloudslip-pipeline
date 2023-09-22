package com.cloudslip.pipeline.updated.dto.kubeconfig;

public class PodResourceSpec {

    private PodResource request;
    private PodResource limit;

    public PodResourceSpec() {
    }

    public PodResourceSpec(int maxCpu, int maxMemory) {
        this.request = new PodResource(maxCpu / 2, maxMemory / 2);
        this.limit = new PodResource(maxCpu, maxMemory);
    }

    public PodResourceSpec(PodResource request, PodResource limit) {
        this.request = request;
        this.limit = limit;
    }

    public PodResource getRequest() {
        return request;
    }

    public void setRequest(PodResource request) {
        this.request = request;
    }

    public PodResource getLimit() {
        return limit;
    }

    public void setLimit(PodResource limit) {
        this.limit = limit;
    }

}
