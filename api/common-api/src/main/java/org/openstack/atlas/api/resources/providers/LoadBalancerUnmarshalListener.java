package org.openstack.atlas.api.resources.providers;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;


import javax.xml.bind.Unmarshaller.Listener;


public class LoadBalancerUnmarshalListener extends Listener {

    public void beforeUnmarshal(Object target, Object parent) {
        if (target instanceof LoadBalancer) {
             LoadBalancer x = (LoadBalancer)(target);
             if (x.getVirtualIps().isEmpty())  {

             }

        }
    }
}
