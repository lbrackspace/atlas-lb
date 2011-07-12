package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "account_limits")
public class AccountLimit extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
 
    @Column(name = "account_id", nullable = false)
    private int accountId;

    @Column(name = "limit_amount", nullable = false)
    private int limit;

    @ManyToOne
    @JoinColumn(name = "limit_type")
    private LimitType limitType;

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String toString(){
        String format = "{id=\"%s\" accountId=\"%d\" limit=\"%d\" limitType=\"%s\" userName=\"%s\"";
        String tid = (getId()==null)?"null":getId().toString();
        String tlimitType = (this.limitType==null)?"null":this.limitType.toString();
        String tuserName = (getUserName()==null)?"null":getUserName();
        return String.format(format,tid,this.accountId,this.limit,tlimitType,tuserName);
    }
}




