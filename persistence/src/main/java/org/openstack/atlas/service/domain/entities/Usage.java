package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name="lb_usage")
public class Usage extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name="loadbalancer_id", nullable = false)
    private LoadBalancer loadbalancer;
    @Column(name = "avg_concurrent_conns", nullable = false)
    Double averageConcurrentConnections = 0.0;
    @Column(name = "bandwidth_in", nullable = false)
    private Long incomingTransfer = 0L;
    @Column(name = "bandwidth_out", nullable = false)
    private Long outgoingTransfer = 0L;
    @Column(name = "avg_concurrent_conns_ssl", nullable = false)
    Double averageConcurrentConnectionsSsl = 0.0;
    @Column(name = "bandwidth_in_ssl", nullable = false)
    private Long incomingTransferSsl = 0L;
    @Column(name = "bandwidth_out_ssl", nullable = false)
    private Long outgoingTransferSsl = 0L;
    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;
    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;
    @Column(name = "num_polls", nullable = false)
    Integer numberOfPolls = 0;
    @Column(name = "num_vips", nullable = false)
    Integer numVips = 1;
    @Column(name = "tags_bitmask", nullable = false)
    Integer tags = 0;
    @Column(name = "event_type", nullable = true)
    String eventType;
    @Column(name="account_id", nullable = false)
    private Integer accountId;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public Double getAverageConcurrentConnections() {
        return averageConcurrentConnections;
    }

    public void setAverageConcurrentConnections(Double averageConcurrentConnections) {
        this.averageConcurrentConnections = averageConcurrentConnections;
    }

    public Long getIncomingTransfer() {
        return incomingTransfer;
    }

    public void setIncomingTransfer(Long incomingTransfer) {
        this.incomingTransfer = incomingTransfer;
    }

    public Long getOutgoingTransfer() {
        return outgoingTransfer;
    }

    public void setOutgoingTransfer(Long outgoingTransfer) {
        this.outgoingTransfer = outgoingTransfer;
    }

    public Double getAverageConcurrentConnectionsSsl() {
        return averageConcurrentConnectionsSsl;
    }

    public void setAverageConcurrentConnectionsSsl(Double averageConcurrentConnectionsSsl) {
        this.averageConcurrentConnectionsSsl = averageConcurrentConnectionsSsl;
    }

    public Long getIncomingTransferSsl() {
        return incomingTransferSsl;
    }

    public void setIncomingTransferSsl(Long incomingTransferSsl) {
        this.incomingTransferSsl = incomingTransferSsl;
    }

    public Long getOutgoingTransferSsl() {
        return outgoingTransferSsl;
    }

    public void setOutgoingTransferSsl(Long outgoingTransferSsl) {
        this.outgoingTransferSsl = outgoingTransferSsl;
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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
