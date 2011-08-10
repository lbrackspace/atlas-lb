package org.openstack.atlas.api.mapper.dozer.factory;

import org.dozer.BeanFactory;
import org.openstack.atlas.core.api.v1.Algorithm;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;

public class LoadBalancerFactory implements BeanFactory {

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId) {
        if (sourceClass.equals(org.openstack.atlas.service.domain.entity.LoadBalancer.class)) {
            LoadBalancer lb = new LoadBalancer();
            lb.setNodes(null);
            lb.setVirtualIps(null);
            return lb;
        }

        if (sourceClass.equals(LoadBalancer.class)) {
            org.openstack.atlas.service.domain.entity.LoadBalancer lb = new org.openstack.atlas.service.domain.entity.LoadBalancer();
            return lb;
        }

        return null;
    }
}
