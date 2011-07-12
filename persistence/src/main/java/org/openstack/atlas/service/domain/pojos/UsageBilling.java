package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.Calendar;

public class UsageBilling implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    private Calendar startTime;
    private Calendar endTime;

    private Integer numVips;
    private Long incomingTransfer;
    private Long outgoingTransfer;

    private boolean ssl;

    private String tier;

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public Integer getNumVips() {
        return numVips;
    }

    public void setNumVips(Integer numVips) {
        this.numVips = numVips;
    }

    public Long getIncomingTransfer() {
        return incomingTransfer;
    }

    public void setIncomingTransfer(Long incomingTransfer) {
        this.incomingTransfer = incomingTransfer;
    }

    public Long getOutgoingTransfer() {
        return outgoingTransfer;
    }

    public void setOutgoingTransfer(Long outgoingTransfer) {
        this.outgoingTransfer = outgoingTransfer;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
}
