package com.cloudslip.pipeline.updated.model.dummy;

import com.cloudslip.pipeline.updated.model.CheckItem;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class CheckItemForAppCommit implements Serializable{

    @NotNull
    private CheckItem checkItem;

    private boolean checked = false;

    public CheckItemForAppCommit() {
    }

    public CheckItemForAppCommit(@NotNull CheckItem checkItem, boolean checked) {
        this.checkItem = checkItem;
        this.checked = checked;
    }

    public CheckItem getCheckItem() {
        return checkItem;
    }

    public void setCheckItem(CheckItem checkItem) {
        this.checkItem = checkItem;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
