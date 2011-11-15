package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.api.v1.extensions.rax.ConnectionLogging;
import org.openstack.atlas.rax.datamodel.XmlHelper;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class ConnectionLoggingConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (destinationClass == List.class) {
            final Boolean cl = (Boolean) sourceFieldValue;
            List<Object> anies = (List<Object>) existingDestinationFieldValue;
            if (anies == null) anies = new ArrayList<Object>();

            try {
                ConnectionLogging connectionLogging = new ConnectionLogging();
                connectionLogging.setEnabled(cl);
                Node objectNode = XmlHelper.marshall(connectionLogging);
                anies.add(objectNode);
            } catch (Exception e) {
                //LOG.error("Error converting accessList from domain to data model", e);
            }

            return anies;
        }

        if (destinationClass == Boolean.class) {
            org.openstack.atlas.api.v1.extensions.rax.ConnectionLogging _connectionLogging = ExtensionObjectMapper.getAnyElement((List<Object>) sourceFieldValue, org.openstack.atlas.api.v1.extensions.rax.ConnectionLogging.class);
            if (_connectionLogging == null) return null;
            return _connectionLogging.isEnabled();
        }


        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
