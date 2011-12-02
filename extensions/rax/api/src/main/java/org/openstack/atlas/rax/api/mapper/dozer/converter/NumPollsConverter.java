package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class NumPollsConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if(destinationClass == Map.class) {
            Map<QName, String> otherAttributes = (Map<QName, String>)(existingDestinationFieldValue);
            if (otherAttributes == null) otherAttributes = new HashMap<QName, String>();
            otherAttributes.put((new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "numberOfPolls", "rax")), sourceFieldValue.toString());

            return otherAttributes;
        }

        if(destinationClass == Integer.class) {
            return ExtensionObjectMapper.<Integer>getOtherAttribute((Map<QName, String>) sourceFieldValue, "numberOfPolls");
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}