package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "account_usage")
public class AccountUsage extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;
    @Column(name = "num_loadbalancers", nullable = false)
    private Integer numLoadBalancers;
    @Column(name = "num_public_vips", nullable = false)
    private Integer numPublicVips;
    @Column(name = "num_servicenet_vips", nullable = false)
    private Integer numServicenetVips;
    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startTime;
    @Column(name = "needs_pushed", nullable = false)
    private boolean needsPushed;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getNumLoadBalancers() {
        return numLoadBalancers;
    }

    public void setNumLoadBalancers(Integer numLoadBalancers) {
        this.numLoadBalancers = numLoadBalancers;
    }

    public Integer getNumPublicVips() {
        return numPublicVips;
    }

    public void setNumPublicVips(Integer numPublicVips) {
        this.numPublicVips = numPublicVips;
    }

    public Integer getNumServicenetVips() {
        return numServicenetVips;
    }

    public void setNumServicenetVips(Integer numServicenetVips) {
        this.numServicenetVips = numServicenetVips;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public boolean isNeedsPushed() {
        return needsPushed;
    }

    public void setNeedsPushed(boolean needsPushed) {
        this.needsPushed = needsPushed;
    }
}
