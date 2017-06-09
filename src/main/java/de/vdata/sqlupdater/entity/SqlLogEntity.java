package de.vdata.sqlupdater.entity;

import java.util.Date;

public class SqlLogEntity {
    private Date executeDate;
    private String scriptName;
    private String state;
    private long scriptSize;

    public SqlLogEntity(Date executeDate, String scriptName, String state) {
        this.executeDate = executeDate;
        this.scriptName = scriptName;
        this.state = state;
    }

    public SqlLogEntity() {
    }

    public Date getExecuteDate() {
        return this.executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String toString() {
        return this.executeDate + " - " + this.scriptName + " - " + this.state;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getScriptSize() {
        return this.scriptSize;
    }

    public void setScriptSize(long scriptSize) {
        this.scriptSize = scriptSize;
    }
}