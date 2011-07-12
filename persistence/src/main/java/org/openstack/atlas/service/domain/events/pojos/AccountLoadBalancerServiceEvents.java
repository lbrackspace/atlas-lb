package org.openstack.atlas.service.domain.events.pojos;
// org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class AccountLoadBalancerServiceEvents
    implements Serializable
{

    private final static long serialVersionUID = 532512316L;
    protected List<LoadBalancerServiceEvents> loadBalancerServiceEvents;
    protected Integer accountId;

    public List<LoadBalancerServiceEvents> getLoadBalancerServiceEvents() {
        if (loadBalancerServiceEvents == null) {
            loadBalancerServiceEvents = new ArrayList<LoadBalancerServiceEvents>();
        }
        return this.loadBalancerServiceEvents;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer value) {
        this.accountId = value;
    }

}
