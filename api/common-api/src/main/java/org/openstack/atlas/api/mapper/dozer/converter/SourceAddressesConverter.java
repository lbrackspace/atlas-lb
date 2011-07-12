package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

public class SourceAddressesConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Host && destinationClass.equals(SourceAddresses.class)) {
                SourceAddresses sourceAddresses = new SourceAddresses();
                sourceAddresses.setIpv4Public(((Host) sourceFieldValue).getIpv4Public());
                sourceAddresses.setIpv4Servicenet(((Host) sourceFieldValue).getIpv4Servicenet());
                sourceAddresses.setIpv6Public(((Host) sourceFieldValue).getIpv6Public());
                sourceAddresses.setIpv6Servicenet(((Host) sourceFieldValue).getIpv6Servicenet());
            return sourceAddresses;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}