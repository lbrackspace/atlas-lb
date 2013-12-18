package org.openstack.atlas.service.domain.usage.entities;

import org.openstack.atlas.service.domain.events.UsageEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "lb_host_usage")
public class LoadBalancerHostUsage extends BigIntEntity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "account_id")
    private int accountId = 0;

    @Column(name = "loadbalancer_id")
    private int loadbalancerId = 0;

    @Column(name = "host_id")
    private int hostId = 0;

    @Column(name = "bandwidth_out")
    private long outgoingTransfer = 0L;

    @Column(name = "bandwidth_in")
    private long incomingTransfer = 0L;

    @Column(name = "bandwidth_out_ssl")
    private long outgoingTransferSsl = 0L;

    @Column(name = "bandwidth_in_ssl")
    private long incomingTransferSsl = 0L;

    @Column(name = "concurrent_connections")
    private long concurrentConnections = 0;

    @Column(name = "concurrent_connections_ssl")
    private long concurrentConnectionsSsl = 0;

    @Column(name = "poll_time")
    private Calendar pollTime = Calendar.getInstance();

    @Column(name = "tags_bitmask")
    private int tagsBitmask = 0;

    @Column(name = "num_vips")
    private int numVips = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private UsageEvent eventType = null;

    public LoadBalancerHostUsage(){

    }

    public LoadBalancerHostUsage(int accountId, int loadbalancerId, int hostId, long outgoingTransfer,
                                 long incomingTransfer, long outgoingTransferSsl, long incomingTransferSsl,
                                 long concurrentConnections, long concurrentConnectionsSsl, int numVips,
                                 int tagsBitmask, Calendar pollTime, UsageEvent eventType){
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.hostId = hostId;
        this.outgoingTransfer = outgoingTransfer;
        this.incomingTransfer =incomingTransfer;
        this.outgoingTransferSsl = outgoingTransferSsl;
        this.incomingTransferSsl = incomingTransferSsl;
        this.concurrentConnections = concurrentConnections;
        this.concurrentConnectionsSsl = concurrentConnectionsSsl;
        this.numVips = numVips;
        this.tagsBitmask = tagsBitmask;
        this.pollTime = pollTime;
        this.eventType = eventType;
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

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }
}