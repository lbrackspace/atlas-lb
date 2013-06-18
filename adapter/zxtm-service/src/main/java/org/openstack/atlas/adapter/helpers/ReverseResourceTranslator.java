package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

/**
 * Created with IntelliJ IDEA.
 * User: adam6424
 * Date: 6/17/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReverseResourceTranslator {
    public static LoadBalancer getLoadBalancer(Integer loadBalancerID, Integer accountID) throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        StingrayRestClient client = new StingrayRestClient();
        LoadBalancer lb = new LoadBalancer();
        String vsName;
        VirtualServer vs;
        VirtualServerProperties vsp;

        lb.setId(loadBalancerID);
        lb.setAccountId(accountID);

        vsName = ZxtmNameBuilder.genVSName(lb);
        lb.setName(vsName);

        vs = client.getVirtualServer(vsName);
        vsp = vs.getProperties();

        return lb;
    }
}
