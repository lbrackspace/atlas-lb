package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class AverageConnectionsConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if(destinationClass == Map.class) {
            Map<QName, String> otherAttributes = (Map<QName, String>)(existingDestinationFieldValue);
            if (otherAttributes == null) otherAttributes = new HashMap<QName, String>();

            otherAttributes.put((new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "averageConcurrentConnections", "rax")), sourceFieldValue.toString());

            return otherAttributes;
        }

        if(destinationClass == Double.class) {
            return ExtensionObjectMapper.<Double>getOtherAttribute((Map<QName, String>) sourceFieldValue, "averageConcurrentConnections");
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
