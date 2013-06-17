package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancer;

/**
 * Created with IntelliJ IDEA.
 * User: adam6424
 * Date: 6/17/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReverseResourceTranslator {
    public static LoadBalancer getLoadBalancer(Integer loadBalancerID, Integer accountID) {
        LoadBalancer lb = new LoadBalancer();

        lb.setId(loadBalancerID);
        lb.setAccountId(accountID);

        // I was going to work on this and realized I'm not sure where to pull this data from
        // I'll take a look again tomorrow

        return lb;
    }
}
