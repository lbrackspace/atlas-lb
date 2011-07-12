
package org.openstack.atlas.service.domain.pojos;
import org.openstack.atlas.service.domain.entities.AccountUsage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AccountBilling implements Serializable {
    private final static long serialVersionUID = 532512316L;
    private Integer accountId;
    private List<AccountUsage> accountUsageRecords = new ArrayList<AccountUsage>();
    private List<LoadBalancerBilling> loadBalancerBillings = new ArrayList<LoadBalancerBilling>();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public List<AccountUsage> getAccountUsageRecords() {
        return accountUsageRecords;
    }

    public void setAccountUsageRecords(List<AccountUsage> accountUsageRecords) {
        this.accountUsageRecords = accountUsageRecords;
    }

    public List<LoadBalancerBilling> getLoadBalancerBillings() {
        return loadBalancerBillings;
    }

    public void setLoadBalancerBillings(List<LoadBalancerBilling> loadBalancerBillings) {
        this.loadBalancerBillings = loadBalancerBillings;
    }
}
