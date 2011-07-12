package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "account_group")
public class AccountGroup extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupRateLimit groupRateLimit;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public GroupRateLimit getGroupRateLimit() {
        return groupRateLimit;
    }

    public void setGroupRateLimit(GroupRateLimit groupRateLimit) {
        this.groupRateLimit = groupRateLimit;
    }
}
