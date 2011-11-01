package org.openstack.atlas.rax.domain.pojo;

import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;

import java.io.Serializable;

public class RaxMessageDataContainer extends MessageDataContainer implements Serializable {
    private final static long serialVersionUID = 532512316L;

    private RaxLoadBalancer raxLoadBalancer;

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        if(loadBalancer instanceof RaxLoadBalancer) this.raxLoadBalancer = (RaxLoadBalancer) loadBalancer;
        else super.setLoadBalancer(loadBalancer);
    }

    public LoadBalancer getLoadBalancer() {
        if(raxLoadBalancer != null) return raxLoadBalancer;
        else return super.getLoadBalancer();
    }
}
