package org.openstack.atlas.service.domain.usage.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "lb_usage")
public class LoadBalancerUsage extends Entity implements Serializable {
    private final static long serialVersionUID = 532512317L;

    @Column(name = "account_id", nullable = false)
    Integer accountId;
    @Column(name = "loadbalancer_id", nullable = false)
    Integer loadbalancerId;
    @Column(name = "avg_concurrent_conns", nullable = false)
    Double averageConcurrentConnections = 0.0;
    @Column(name = "cum_bandwidth_bytes_in", nullable = false)
    Long cumulativeBandwidthBytesIn = 0L;
    @Column(name = "cum_bandwidth_bytes_out", nullable = false)
    Long cumulativeBandwidthBytesOut = 0L;
    @Column(name = "last_bandwidth_bytes_in", nullable = false)
    Long lastBandwidthBytesIn = 0L;
    @Column(name = "last_bandwidth_bytes_out", nullable = false)
    Long lastBandwidthBytesOut = 0L;
    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Calendar startTime;
    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Calendar endTime;
    @Column(name = "num_polls", nullable = false)
    Integer numberOfPolls = 0;
    @Column(name = "num_vips", nullable = false)
    Integer numVips = 1;
    @Column(name = "tags_bitmask", nullable = false)
    Integer tags = 0;
    @Column(name = "event_type", nullable = true)
    String eventType;


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

    public Double getAverageConcurrentConnections() {
        return averageConcurrentConnections;
    }

    public void setAverageConcurrentConnections(Double averageConcurrentConnections) {
        this.averageConcurrentConnections = averageConcurrentConnections;
    }

    public Long getCumulativeBandwidthBytesIn() {
        return cumulativeBandwidthBytesIn;
    }

    public void setCumulativeBandwidthBytesIn(Long cumulativeBandwidthBytesIn) {
        this.cumulativeBandwidthBytesIn = cumulativeBandwidthBytesIn;
    }

    public Long getCumulativeBandwidthBytesOut() {
        return cumulativeBandwidthBytesOut;
    }

    public void setCumulativeBandwidthBytesOut(Long cumulativeBandwidthBytesOut) {
        this.cumulativeBandwidthBytesOut = cumulativeBandwidthBytesOut;
    }

    public Long getLastBandwidthBytesIn() {
        return lastBandwidthBytesIn;
    }

    public void setLastBandwidthBytesIn(Long lastBandwidthBytesIn) {
        this.lastBandwidthBytesIn = lastBandwidthBytesIn;
    }

    public Long getLastBandwidthBytesOut() {
        return lastBandwidthBytesOut;
    }

    public void setLastBandwidthBytesOut(Long lastBandwidthBytesOut) {
        this.lastBandwidthBytesOut = lastBandwidthBytesOut;
    }

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

    public Integer getNumberOfPolls() {
        return numberOfPolls;
    }

    public void setNumberOfPolls(Integer numberOfPolls) {
        this.numberOfPolls = numberOfPolls;
    }

    public Integer getNumVips() {
        return numVips;
    }

    public void setNumVips(Integer numVips) {
        this.numVips = numVips;
    }

    public Integer getTags() {
        return tags;
    }

    public void setTags(Integer tags) {
        this.tags = tags;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
