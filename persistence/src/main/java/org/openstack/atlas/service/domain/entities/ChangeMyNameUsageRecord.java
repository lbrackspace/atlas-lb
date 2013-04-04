package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "access_list")
public class ChangeMyNameUsageRecord extends Entity implements Serializable {

    @Column(name = "account_id")
    private int accountId;

    @Column(name = "loadbalancer_id")
    private int loadbalancerId;

    @Column(name = "bandwidth_out")
    private long outgoingTransfer;

    @Column(name = "bandwidth_in")
    private long incomingTransfer;

    @Column(name = "bandwidth_out_ssl")
    private long outgoingTransferSsl;

    @Column(name = "bandwidth_in_ssl")
    private long incomingTransferSsl;

    @Column(name = "concurrent_connections")
    private long averageConcurrentConnections;

    @Column(name = "concurrent_connections_ssl")
    private long averageConcurrentConnectionsSsl;

    @Column(name = "poll_time")
    private Calendar pollTime;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private String eventType;

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

    public long getAverageConcurrentConnections() {
        return averageConcurrentConnections;
    }

    public void setAverageConcurrentConnections(long averageConcurrentConnections) {
        this.averageConcurrentConnections = averageConcurrentConnections;
    }

    public long getAverageConcurrentConnectionsSsl() {
        return averageConcurrentConnectionsSsl;
    }

    public void setAverageConcurrentConnectionsSsl(long averageConcurrentConnectionsSsl) {
        this.averageConcurrentConnectionsSsl = averageConcurrentConnectionsSsl;
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

}