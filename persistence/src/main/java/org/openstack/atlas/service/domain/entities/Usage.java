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
    @Column(name="entry_version", nullable = false)
    private Integer entryVersion;
    @Column(name = "needs_pushed", nullable = false)
    private boolean needsPushed;
    @Column(name = "uuid", nullable = true)
    private String uuid;
    @Column(name = "corrected", nullable = false)
    private boolean corrected;
    @Column(name = "num_attempts", nullable = false)
    private int numAttempts;

    public Usage() {
    }

    public Usage(LoadBalancer loadbalancer, Double averageConcurrentConnections, Long incomingTransfer, Long outgoingTransfer, Double averageConcurrentConnectionsSsl, Long incomingTransferSsl, Long outgoingTransferSsl, Calendar startTime, Calendar endTime, Integer numberOfPolls, Integer numVips, Integer tags, String eventType, Integer accountId, Integer entryVersion, boolean needsPushed, String uuid) {
        this.loadbalancer = loadbalancer;
        this.averageConcurrentConnections = averageConcurrentConnections;
        this.incomingTransfer = incomingTransfer;
        this.outgoingTransfer = outgoingTransfer;
        this.averageConcurrentConnectionsSsl = averageConcurrentConnectionsSsl;
        this.incomingTransferSsl = incomingTransferSsl;
        this.outgoingTransferSsl = outgoingTransferSsl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfPolls = numberOfPolls;
        this.numVips = numVips;
        this.tags = tags;
        this.eventType = eventType;
        this.accountId = accountId;
        this.entryVersion = entryVersion;
        this.needsPushed = needsPushed;
        this.uuid = uuid;
    }

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

    public Integer getEntryVersion() {
        return entryVersion;
    }

    public void setEntryVersion(Integer entryVersion) {
        this.entryVersion = entryVersion;
    }

    public boolean isNeedsPushed() {
        return needsPushed;
    }

    public void setNeedsPushed(boolean needsPushed) {
        this.needsPushed = needsPushed;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isCorrected() {
        return corrected;
    }

    public void setCorrected(boolean corrected) {
        this.corrected = corrected;
    }

    public int getNumAttempts() {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts) {
        this.numAttempts = numAttempts;
    }

    public static Usage createNullUsageRecord() {
        Usage currUsageRecord = new Usage();
        currUsageRecord.setAccountId(null);
        currUsageRecord.setLoadbalancer(null);
        currUsageRecord.setAverageConcurrentConnections(null);
        currUsageRecord.setAverageConcurrentConnectionsSsl(null);
        currUsageRecord.setIncomingTransfer(null);
        currUsageRecord.setIncomingTransferSsl(null);
        currUsageRecord.setOutgoingTransfer(null);
        currUsageRecord.setOutgoingTransferSsl(null);
        currUsageRecord.setNumberOfPolls(0);
        currUsageRecord.setNumVips(null);
        currUsageRecord.setTags(0);
        currUsageRecord.setEventType(null);
        currUsageRecord.setUuid(null);
        return currUsageRecord;
    }
}
