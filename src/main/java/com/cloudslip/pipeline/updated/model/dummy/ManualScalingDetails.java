package com.cloudslip.pipeline.updated.model.dummy;

import java.io.Serializable;

public class ManualScalingDetails implements Serializable {

    private int numberOfInstances;

    public ManualScalingDetails() {
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
}
