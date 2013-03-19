package org.openstack.atlas.usagerefactor;

import org.joda.time.DateTime;

import java.util.Calendar;

public class PolledUsageRecord {
    private int id;
    private int accountId;
    private int loadbalancerId;
    private long bandwidthOut;
    private long bandwidthIn;
    private long bandwidthOutSsl;
    private long bandwidthInSsl;
    private Calendar pollTime;
    private int numConnections;
    private String eventType;

    public PolledUsageRecord(int id, int accountId, int loadbalancerId, long bandwidthOut, long bandwidthIn, long bandwidthOutSsl, long bandwidthInSsl, Calendar pollTime, int numConnections, String eventType) {
        this.id = id;
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.bandwidthOut = bandwidthOut;
        this.bandwidthIn = bandwidthIn;
        this.bandwidthOutSsl = bandwidthOutSsl;
        this.bandwidthInSsl = bandwidthInSsl;
        this.pollTime = pollTime;
        this.numConnections = numConnections;
        this.eventType = eventType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public long getBandwidthOut() {
        return bandwidthOut;
    }

    public void setBandwidthOut(long bandwidthOut) {
        this.bandwidthOut = bandwidthOut;
    }

    public long getBandwidthOutSsl() {
        return bandwidthOutSsl;
    }

    public void setBandwidthOutSsl(long bandwidthOutSsl) {
        this.bandwidthOutSsl = bandwidthOutSsl;
    }

    public long getBandwidthIn() {
        return bandwidthIn;
    }

    public void setBandwidthIn(long bandwidthIn) {
        this.bandwidthIn = bandwidthIn;
    }

    public long getBandwidthInSsl() {
        return bandwidthInSsl;
    }

    public void setBandwidthInSsl(long bandwidthInSsl) {
        this.bandwidthInSsl = bandwidthInSsl;
    }

    public int getNumConnections() {
        return numConnections;
    }

    public void setNumConnections(int numConnections) {
        this.numConnections = numConnections;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Calendar getPollTime() {
        return pollTime;
    }

    public void setPollTime(Calendar pollTime) {
        this.pollTime = pollTime;
    }
}
