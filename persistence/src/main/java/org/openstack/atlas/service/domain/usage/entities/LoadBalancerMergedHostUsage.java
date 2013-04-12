package org.openstack.atlas.service.domain.usage.entities;

import org.openstack.atlas.service.domain.events.UsageEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
public class LoadBalancerMergedHostUsage implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private int id;
    @Column(name = "account_id", nullable = false)
    private int accountId;
    @Column(name = "loadbalancer_id", nullable = false)
    private int loadbalancerId;
    @Column(name = "outgoing_transfer", nullable = false)
    private long outgoingTransfer;
    @Column(name = "incoming_transfer", nullable = false)
    private long incomingTransfer;
    @Column(name = "outgoing_transfer_ssl", nullable = false)
    private long outgoingTransferSsl;
    @Column(name = "incoming_transfer_ssl", nullable = false)
    private long incomingTransferSsl;
    @Column(name = "concurrent_connections", nullable = false)
    private double concurrentConnections;
    @Column(name = "concurrent_connections_ssl", nullable = false)
    private double concurrentConnectionsSsl;
    @Column(name = "num_vips", nullable = false)
    private int numVips;
    @Column(name = "tags_bitmask", nullable = false)
    private int tagsBitmask;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "poll_time", nullable = false)
    private Calendar pollTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = true)
    private UsageEvent eventType;

    public LoadBalancerMergedHostUsage() {

    }

    public LoadBalancerMergedHostUsage(int id, int accountId, int loadbalancerId, long outgoingTransfer,
                                       long incomingTransfer, long outgoingTransferSsl, long incomingTransferSsl,
                                       long concurrentConnections, long concurrentConnectionsSsl,
                                       int numVips, int tagsBitmask, Calendar pollTime, UsageEvent eventType) {
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

    public UsageEvent getEventType() {
        return eventType;
    }

    public void setEventType(UsageEvent eventType) {
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
