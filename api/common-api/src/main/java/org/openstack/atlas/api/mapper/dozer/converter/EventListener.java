package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.dozer.DozerEventListener;
import org.dozer.event.DozerEvent;

public class EventListener implements DozerEventListener {

    @Override
    public void mappingStarted(DozerEvent dozerEvent) {
        // Not implemented
    }

    @Override
    public void preWritingDestinationValue(DozerEvent dozerEvent) {
        // Not implemented
    }

    @Override
    public void postWritingDestinationValue(DozerEvent dozerEvent) {
        // Not implemented
    }

    @Override
    public void mappingFinished(DozerEvent dozerEvent) {
        if(dozerEvent.getDestinationObject() instanceof LoadBalancer) {
            LoadBalancer lb = (LoadBalancer) dozerEvent.getDestinationObject();

            if(lb.getVirtualIps() != null && lb.getVirtualIps().isEmpty()) lb.setVirtualIps(null);
            if(lb.getNodes() != null && lb.getNodes().isEmpty()) lb.setNodes(null);
            if(lb.getMetadata() != null && lb.getMetadata().isEmpty()) lb.setMetadata(null);
            if(lb.getLoadBalancerUsage() != null && lb.getLoadBalancerUsage().getLoadBalancerUsageRecords().isEmpty()) lb.setLoadBalancerUsage(null);
            if(lb.getAccessList() != null && lb.getAccessList().isEmpty()) lb.setAccessList(null);
        }
    }
}
