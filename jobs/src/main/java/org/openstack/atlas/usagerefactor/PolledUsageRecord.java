package org.openstack.atlas.usagerefactor;

import java.util.Calendar;

public class PolledUsageRecord {
    private int id;
    private int accountId;
    private int loadbalancerId;
    private long outgoingTransfer;
    private long incomingTransfer;
    private long outgoingTransferSsl;
    private long incomingTransferSsl;
    private double concurrentConnections;
    private double concurrentConnectionsSsl;
    private int numVips;
    private int tagsBitmask;
    private Calendar pollTime;
    private String eventType;

    public PolledUsageRecord(int id, int accountId, int loadbalancerId, long outgoingTransfer,
                             long incomingTransfer, long outgoingTransferSsl, long incomingTransferSsl,
                             long concurrentConnections, long concurrentConnectionsSsl,
                             int numVips, int tagsBitmask, Calendar pollTime, String eventType) {
        this.id = id;
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.outgoingTransfer = outgoingTransfer;
        this.incomingTransfer = incomingTransfer;
        this.outgoingTransferSsl = outgoingTransferSsl;
        this.incomingTransferSsl = incomingTransferSsl;
        this.concurrentConnections = concurrentConnections;
        this.concurrentConnectionsSsl = concurrentConnectionsSsl;
        this.numVips = numVips;
        this.tagsBitmask = tagsBitmask;
        this.pollTime = pollTime;
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

    public long getOutgoingTransfer() {
        return outgoingTransfer;
    }

    public void setOutgoingTransfer(long outgoingTransfer) {
        this.outgoingTransfer = outgoingTransfer;
    }

    public long getIncomingTransfer() {
        return incomingTransfer;
    }

    public void setIncomingTransfer(long incomingTransfer) {
        this.incomingTransfer = incomingTransfer;
    }

    public long getOutgoingTransferSsl() {
        return outgoingTransferSsl;
    }

    public void setOutgoingTransferSsl(long outgoingTransferSsl) {
        this.outgoingTransferSsl = outgoingTransferSsl;
    }

    public long getIncomingTransferSsl() {
        return incomingTransferSsl;
    }

    public void setIncomingTransferSsl(long incomingTransferSsl) {
        this.incomingTransferSsl = incomingTransferSsl;
    }

    public double getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(double concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public double getConcurrentConnectionsSsl() {
        return concurrentConnectionsSsl;
    }

    public void setConcurrentConnectionsSsl(double concurrentConnectionsSsl) {
        this.concurrentConnectionsSsl = concurrentConnectionsSsl;
    }

    public Calendar getPollTime() {
        return pollTime;
    }

    public void setPollTime(Calendar pollTime) {
        this.pollTime = pollTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getNumVips() {
        return numVips;
    }

    public void setNumVips(int numVips) {
        this.numVips = numVips;
    }

    public int getTagsBitmask() {
        return tagsBitmask;
    }

    public void setTagsBitmask(int tagsBitmask) {
        this.tagsBitmask = tagsBitmask;
    }
}
