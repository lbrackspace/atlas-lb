package org.openstack.atlas.rax.api.mapper.dozer.factory;

import org.dozer.BeanFactory;
import org.openstack.atlas.core.api.v1.LoadBalancer;

public class RaxLoadBalancerFactory implements BeanFactory {

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId) {
//        if (source instanceof org.openstack.atlas.service.domain.entity.LoadBalancer) {
        if (sourceClass.equals(org.openstack.atlas.rax.domain.entity.RaxLoadBalancer.class)
                || sourceClass.equals(org.openstack.atlas.service.domain.entity.LoadBalancer.class)) {
            LoadBalancer lb = new LoadBalancer();
            lb.setNodes(null);
            lb.setVirtualIps(null);
            return lb;
        }

        if (sourceClass.equals(LoadBalancer.class)) {
            org.openstack.atlas.rax.domain.entity.RaxLoadBalancer lb = new org.openstack.atlas.rax.domain.entity.RaxLoadBalancer();
            return lb;
        }

        return null;
    }
}
