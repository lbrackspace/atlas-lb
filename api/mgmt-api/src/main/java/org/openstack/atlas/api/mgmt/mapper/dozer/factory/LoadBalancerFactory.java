package org.openstack.atlas.api.mgmt.mapper.dozer.factory;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.dozer.BeanFactory;

public class LoadBalancerFactory implements BeanFactory {

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId) {

        if (sourceClass.equals(org.openstack.atlas.service.domain.entities.LoadBalancer.class)) {
            LoadBalancer lb = new LoadBalancer();
            lb.setAccessList(null);
            lb.setLoadBalancerUsage(null);
            lb.setNodes(null);
            lb.setMetadata(null);
            lb.setVirtualIps(null);
            lb.setTickets(null);
            return lb;
        }

        if (sourceClass.equals(LoadBalancer.class)) {
            org.openstack.atlas.service.domain.entities.LoadBalancer lb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            return lb;
        }

        return null;
    }
}
