package org.openstack.atlas.api.mapper.dozer.factory;

import org.dozer.BeanFactory;
import org.dozer.config.BeanContainer;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;

public class NodeFactory implements BeanFactory {

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId, BeanContainer beanContainer) {

        if (sourceClass.equals(org.openstack.atlas.service.domain.entities.Node.class)) {
            Node node = new Node();
            node.setId(null);
            node.setAddress(null);
            node.setCondition(null);
            node.setMetadata(null);
            node.setPort(null);
            node.setStatus(null);
            node.setType(null);
            node.setWeight(null);
            return node;
        }

        if (sourceClass.equals(Node.class)) {
            return new org.openstack.atlas.service.domain.entities.Node();
        }

        return null;
    }
}