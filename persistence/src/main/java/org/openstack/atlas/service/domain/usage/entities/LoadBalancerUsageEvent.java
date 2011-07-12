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
}
