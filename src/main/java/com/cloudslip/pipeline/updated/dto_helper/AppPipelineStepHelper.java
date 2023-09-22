package com.cloudslip.pipeline.updated.dto_helper;

import org.bson.types.ObjectId;

public class AppPipelineStepHelper {
    private ObjectId id;
    private String triggerMode;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(String triggerMode) {
        this.triggerMode = triggerMode;
    }
}
