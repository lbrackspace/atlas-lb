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
    @Column(name = "last_bandwidth_bytes_in", nullable = true)
    Long lastBandwidthBytesIn;
    @Column(name = "last_bandwidth_bytes_out", nullable = true)
    Long lastBandwidthBytesOut;
    @Column(name = "avg_concurrent_conns_ssl", nullable = false)
    Double averageConcurrentConnectionsSsl = 0.0;
    @Column(name = "cum_bandwidth_bytes_in_ssl", nullable = false)
    Long cumulativeBandwidthBytesInSsl = 0L;
    @Column(name = "cum_bandwidth_bytes_out_ssl", nullable = false)
    Long cumulativeBandwidthBytesOutSsl = 0L;
    @Column(name = "last_bandwidth_bytes_in_ssl", nullable = true)
    Long lastBandwidthBytesInSsl;
    @Column(name = "last_bandwidth_bytes_out_ssl", nullable = true)
    Long lastBandwidthBytesOutSsl;
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

    public LoadBalancerUsage() {
    }

    public LoadBalancerUsage(Integer accountId, Integer loadbalancerId, Double averageConcurrentConnections, Long cumulativeBandwidthBytesIn, Long cumulativeBandwidthBytesOut, Long lastBandwidthBytesIn, Long lastBandwidthBytesOut, Double averageConcurrentConnectionsSsl, Long cumulativeBandwidthBytesInSsl, Long cumulativeBandwidthBytesOutSsl, Long lastBandwidthBytesInSsl, Long lastBandwidthBytesOutSsl, Calendar startTime, Calendar endTime, Integer numberOfPolls, Integer numVips, Integer tags, String eventType) {
        this.accountId = accountId;
        this.loadbalancerId = loadbalancerId;
        this.averageConcurrentConnections = averageConcurrentConnections;
        this.cumulativeBandwidthBytesIn = cumulativeBandwidthBytesIn;
        this.cumulativeBandwidthBytesOut = cumulativeBandwidthBytesOut;
        this.lastBandwidthBytesIn = lastBandwidthBytesIn;
        this.lastBandwidthBytesOut = lastBandwidthBytesOut;
        this.averageConcurrentConnectionsSsl = averageConcurrentConnectionsSsl;
        this.cumulativeBandwidthBytesInSsl = cumulativeBandwidthBytesInSsl;
        this.cumulativeBandwidthBytesOutSsl = cumulativeBandwidthBytesOutSsl;
        this.lastBandwidthBytesInSsl = lastBandwidthBytesInSsl;
        this.lastBandwidthBytesOutSsl = lastBandwidthBytesOutSsl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfPolls = numberOfPolls;
        this.numVips = numVips;
        this.tags = tags;
        this.eventType = eventType;
    }

    public LoadBalancerUsage(LoadBalancerUsage other) {
        this.accountId = other.accountId;
        this.loadbalancerId = other.loadbalancerId;
        this.averageConcurrentConnections = other.averageConcurrentConnections;
        this.cumulativeBandwidthBytesIn = other.cumulativeBandwidthBytesIn;
        this.cumulativeBandwidthBytesOut = other.cumulativeBandwidthBytesOut;
        this.lastBandwidthBytesIn = other.lastBandwidthBytesIn;
        this.lastBandwidthBytesOut = other.lastBandwidthBytesOut;
        this.averageConcurrentConnectionsSsl = other.averageConcurrentConnectionsSsl;
        this.cumulativeBandwidthBytesInSsl = other.cumulativeBandwidthBytesInSsl;
        this.cumulativeBandwidthBytesOutSsl = other.cumulativeBandwidthBytesOutSsl;
        this.lastBandwidthBytesInSsl = other.lastBandwidthBytesInSsl;
        this.lastBandwidthBytesOutSsl = other.lastBandwidthBytesOutSsl;
        this.startTime = (Calendar) other.startTime.clone();
        this.endTime = (Calendar) other.endTime.clone();
        this.numberOfPolls = other.numberOfPolls;
        this.numVips = other.numVips;
        this.tags = other.tags;
        this.eventType = other.eventType;
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

    public Double getAverageConcurrentConnectionsSsl() {
        return averageConcurrentConnectionsSsl;
    }

    public void setAverageConcurrentConnectionsSsl(Double averageConcurrentConnectionsSsl) {
        this.averageConcurrentConnectionsSsl = averageConcurrentConnectionsSsl;
    }

    public Long getCumulativeBandwidthBytesInSsl() {
        return cumulativeBandwidthBytesInSsl;
    }

    public void setCumulativeBandwidthBytesInSsl(Long cumulativeBandwidthBytesInSsl) {
        this.cumulativeBandwidthBytesInSsl = cumulativeBandwidthBytesInSsl;
    }

    public Long getCumulativeBandwidthBytesOutSsl() {
        return cumulativeBandwidthBytesOutSsl;
    }

    public void setCumulativeBandwidthBytesOutSsl(Long cumulativeBandwidthBytesOutSsl) {
        this.cumulativeBandwidthBytesOutSsl = cumulativeBandwidthBytesOutSsl;
    }

    public Long getLastBandwidthBytesInSsl() {
        return lastBandwidthBytesInSsl;
    }

    public void setLastBandwidthBytesInSsl(Long lastBandwidthBytesInSsl) {
        this.lastBandwidthBytesInSsl = lastBandwidthBytesInSsl;
    }

    public Long getLastBandwidthBytesOutSsl() {
        return lastBandwidthBytesOutSsl;
    }

    public void setLastBandwidthBytesOutSsl(Long lastBandwidthBytesOutSsl) {
        this.lastBandwidthBytesOutSsl = lastBandwidthBytesOutSsl;
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

    @Override
    public String toString() {
        return "LoadBalancerUsage{" +
                "accountId=" + accountId +
                ", loadbalancerId=" + loadbalancerId +
                ", averageConcurrentConnections=" + averageConcurrentConnections +
                ", cumulativeBandwidthBytesIn=" + cumulativeBandwidthBytesIn +
                ", cumulativeBandwidthBytesOut=" + cumulativeBandwidthBytesOut +
                ", lastBandwidthBytesIn=" + lastBandwidthBytesIn +
                ", lastBandwidthBytesOut=" + lastBandwidthBytesOut +
                ", averageConcurrentConnectionsSsl=" + averageConcurrentConnectionsSsl +
                ", cumulativeBandwidthBytesInSsl=" + cumulativeBandwidthBytesInSsl +
                ", cumulativeBandwidthBytesOutSsl=" + cumulativeBandwidthBytesOutSsl +
                ", lastBandwidthBytesInSsl=" + lastBandwidthBytesInSsl +
                ", lastBandwidthBytesOutSsl=" + lastBandwidthBytesOutSsl +
                ", startTime=" + startTime.getTime() +
                ", endTime=" + endTime.getTime() +
                ", numberOfPolls=" + numberOfPolls +
                ", numVips=" + numVips +
                ", tags=" + tags +
                ", eventType='" + eventType + '\'' +
                "} " + super.toString();
    }

}
