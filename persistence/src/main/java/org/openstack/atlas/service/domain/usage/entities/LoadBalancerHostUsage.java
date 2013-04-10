package org.openstack.atlas.service.domain.usage.entities;

import org.openstack.atlas.service.domain.events.UsageEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "access_list")
public class LoadBalancerHostUsage extends org.openstack.atlas.service.domain.entities.Entity implements Serializable {

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
    private long concurrentConnections;

    @Column(name = "concurrent_connections_ssl")
    private long concurrentConnectionsSsl;

    @Column(name = "poll_time")
    private Calendar pollTime;

    @Column(name = "tags_bitmask")
    private int tagsBitmask;

    @Column(name = "num_vips")
    private int numVips;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private UsageEvent eventType;

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

    public long getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(long concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public long getConcurrentConnectionsSsl() {
        return concurrentConnectionsSsl;
    }

    public void setConcurrentConnectionsSsl(long concurrentConnectionsSsl) {
        this.concurrentConnectionsSsl = concurrentConnectionsSsl;
    }

    public Calendar getPollTime() {
        return pollTime;
    }

    public void setPollTime(Calendar pollTime) {
        this.pollTime = pollTime;
    }

    public int getTagsBitmask() {
        return tagsBitmask;
    }

    public void setTagsBitmask(int tagsBitmask) {
        this.tagsBitmask = tagsBitmask;
    }

    public UsageEvent getEventType() {
        return eventType;
    }

    public void setEventType(UsageEvent eventType) {
        this.eventType = eventType;
    }

    public int getNumVips() {
        return numVips;
    }

    public void setNumVips(int numVips) {
        this.numVips = numVips;
    }
}