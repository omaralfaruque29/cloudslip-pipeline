package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.io.Serializable;

public class PodResource implements Serializable {

    private int cpu;
    private int memory;

    public PodResource() {
    }

    public PodResource(int cpu, int memory) {
        this.cpu = cpu;
        this.memory = memory;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }
}
