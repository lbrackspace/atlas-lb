package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name="load_balancer_usage")
public class UsageRecord extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name="load_balancer_id", nullable = false)
    private LoadBalancer loadBalancer;

    @Column(name = "transfer_bytes_in", nullable = false)
    private Long transferBytesIn;

    @Column(name = "transfer_bytes_out", nullable = false)
    private Long transferBytesOut;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endTime;

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Long getTransferBytesIn() {
        return transferBytesIn;
    }

    public void setTransferBytesIn(Long transferBytesIn) {
        this.transferBytesIn = transferBytesIn;
    }

    public Long getTransferBytesOut() {
        return transferBytesOut;
    }

    public void setTransferBytesOut(Long transferBytesOut) {
        this.transferBytesOut = transferBytesOut;
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

    @Override
    public String toString() {
        return "UsageRecord{" +
                "endTime=" + endTime +
                ", startTime=" + startTime +
                ", transferBytesOut=" + transferBytesOut +
                ", transferBytesIn=" + transferBytesIn +
                '}';
    }
}
