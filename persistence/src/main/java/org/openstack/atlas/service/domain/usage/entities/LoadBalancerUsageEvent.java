package org.openstack.atlas.service.domain.usage.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "lb_usage_event")
public class LoadBalancerUsageEvent extends Entity implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Column(name = "account_id", nullable = false)
    Integer accountId;
    @Column(name = "loadbalancer_id", nullable = false)
    Integer loadbalancerId;
    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Calendar startTime;
    @Column(name = "num_vips", nullable = false)
    Integer numVips = 1;
    @Column(name = "event_type", nullable = true)
    String eventType;
    @Column(name = "last_bandwidth_bytes_out", nullable = true)
    Long lastBandwidthBytesOut;
    @Column(name = "last_bandwidth_bytes_in", nullable = true)
    Long lastBandwidthBytesIn;
    @Column(name = "last_concurrent_conns", nullable = true)
    Integer lastConcurrentConnections;
    @Column(name = "last_bandwidth_bytes_out_ssl", nullable = true)
    Long lastBandwidthBytesOutSsl;
    @Column(name = "last_bandwidth_bytes_in_ssl", nullable = true)
    Long lastBandwidthBytesInSsl;
    @Column(name = "last_concurrent_conns_ssl", nullable = true)
    Integer lastConcurrentConnectionsSsl;

    public LoadBalancerUsageEvent() {
    }

    public LoadBalancerUsageEvent(Integer accountId, Integer loadbalancerId, Calendar startTime, Integer numVips, String eventType, Long lastBandwidthBytesOut, Long lastBandwidthBytesIn, Integer lastConcurrentConnections, Long lastBandwidthBytesOutSsl, Long lastBandwidthBytesInSsl, Integer lastConcurrentConnectionsSsl) {
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.startTime = startTime;
        this.numVips = numVips;
        this.eventType = eventType;
        this.lastBandwidthBytesOut = lastBandwidthBytesOut;
        this.lastBandwidthBytesIn = lastBandwidthBytesIn;
        this.lastConcurrentConnections = lastConcurrentConnections;
        this.lastBandwidthBytesOutSsl = lastBandwidthBytesOutSsl;
        this.lastBandwidthBytesInSsl = lastBandwidthBytesInSsl;
        this.lastConcurrentConnectionsSsl = lastConcurrentConnectionsSsl;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(Integer loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Integer getNumVips() {
        return numVips;
    }

    public void setNumVips(Integer numVips) {
        this.numVips = numVips;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getLastBandwidthBytesOut() {
        return lastBandwidthBytesOut;
    }

    public void setLastBandwidthBytesOut(Long lastBandwidthBytesOut) {
        this.lastBandwidthBytesOut = lastBandwidthBytesOut;
    }

    public Long getLastBandwidthBytesIn() {
        return lastBandwidthBytesIn;
    }

    public void setLastBandwidthBytesIn(Long lastBandwidthBytesIn) {
        this.lastBandwidthBytesIn = lastBandwidthBytesIn;
    }

    public Integer getLastConcurrentConnections() {
        return lastConcurrentConnections;
    }

    public void setLastConcurrentConnections(Integer lastConcurrentConnections) {
        this.lastConcurrentConnections = lastConcurrentConnections;
    }

    public Long getLastBandwidthBytesOutSsl() {
        return lastBandwidthBytesOutSsl;
    }

    public void setLastBandwidthBytesOutSsl(Long lastBandwidthBytesOutSsl) {
        this.lastBandwidthBytesOutSsl = lastBandwidthBytesOutSsl;
    }

    public Long getLastBandwidthBytesInSsl() {
        return lastBandwidthBytesInSsl;
    }

    public void setLastBandwidthBytesInSsl(Long lastBandwidthBytesInSsl) {
        this.lastBandwidthBytesInSsl = lastBandwidthBytesInSsl;
    }

    public Integer getLastConcurrentConnectionsSsl() {
        return lastConcurrentConnectionsSsl;
    }

    public void setLastConcurrentConnectionsSsl(Integer lastConcurrentConnectionsSsl) {
        this.lastConcurrentConnectionsSsl = lastConcurrentConnectionsSsl;
    }
}
