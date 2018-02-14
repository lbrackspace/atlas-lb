package org.openstack.atlas.api.mapper.dozer.factory;

import org.dozer.BeanFactory;
import org.dozer.config.BeanContainer;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;

public class CertificateMappingFactory implements BeanFactory {

    @Override
    public Object createBean(Object source, Class<?> sourceClass, String targetBeanId, BeanContainer beanContainer) {
        if (sourceClass.equals(org.openstack.atlas.service.domain.entities.CertificateMapping.class)) {
            CertificateMapping certificateMapping = new CertificateMapping();
            certificateMapping.setId(null);
            certificateMapping.setPrivateKey(null);
            certificateMapping.setIntermediateCertificate(null);
            certificateMapping.setCertificate(null);
            certificateMapping.setHostName(null);
            return certificateMapping;
        }

        if (sourceClass.equals(CertificateMapping.class)) {
            return new org.openstack.atlas.service.domain.entities.CertificateMapping();
        }

        return null;
    }
}
