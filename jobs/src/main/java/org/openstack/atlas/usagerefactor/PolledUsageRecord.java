package org.openstack.atlas.usagerefactor;

import java.util.Calendar;

public class PolledUsageRecord {
    private int id;
    private int accountId;
    private int loadbalancerId;
    private String ipAddress;
    private String protocol;
    private int port;
    private boolean ssl;
    private long bandwidthOut;
    private long bandwidthIn;
    private Calendar pollTime;
    private long concurrentConnections;
    private String eventType;

    public PolledUsageRecord(int id, int accountId, int loadbalancerId, String ipAddress, String protocol, int port,
                             boolean ssl, long bandwidthOut, long bandwidthIn, Calendar pollTime,
                             long concurrentConnections, String eventType) {
        this.id = id;
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.ipAddress = ipAddress;
        this.protocol = protocol;
        this.port = port;
        this.ssl = ssl;
        this.bandwidthOut = bandwidthOut;
        this.bandwidthIn = bandwidthIn;
        this.pollTime = pollTime;
        this.concurrentConnections = concurrentConnections;
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

    public long getBandwidthIn() {
        return bandwidthIn;
    }

    public void setBandwidthIn(long bandwidthIn) {
        this.bandwidthIn = bandwidthIn;
    }

    public long getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(long concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
