package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Table(name = "lb_status_history")
public class LoadBalancerStatusHistory extends org.openstack.atlas.service.domain.entities.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "loadbalancer_id", nullable = false)
    private Integer loadbalancerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private LoadBalancerStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

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

    public LoadBalancerStatus getStatus() {
        return status;
    }

    public void setStatus(LoadBalancerStatus status) {
        this.status = status;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    @Override
    public String toString(){
        String attrString = getAttributesAsString();
        return String.format("{%s}",attrString);
    }

    public String getAttributesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("id=%s ",vorn(getId())));
        sb.append(String.format("accountId=%s ",vorn(getAccountId())));
        sb.append(String.format("lb_id=%s ",vorn(getLoadbalancerId())));
        sb.append(String.format("created=\"%s\" ",vorn(getCreated())));
        return sb.toString();
    }

    protected String vorn(Object obj) {
        if(obj==null) {
            return "";
        }
        if(obj instanceof Calendar) {
            return ((Calendar)obj).getTime().toString();
        }
        return obj.toString();
    }
}
