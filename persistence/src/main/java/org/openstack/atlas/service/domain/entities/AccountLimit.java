package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "account_limits")
public class AccountLimit extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
 
    @Column(name = "account_id", nullable = false)
    private Integer  accountId;

    @Column(name = "limit_amount", nullable = false)
    private Integer  limit;

    @ManyToOne
    @JoinColumn(name = "limit_type")
    private LimitType limitType;

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    public Integer  getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer  accountId) {
        this.accountId = accountId;
    }

    public Integer  getLimit() {
        return limit;
    }

    public void setLimit(Integer  limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "AccountLimit{" +
                "accountId=" + accountId +
                ", limit=" + limit +
                ", limitType=" + limitType +
                '}';
    }
}




