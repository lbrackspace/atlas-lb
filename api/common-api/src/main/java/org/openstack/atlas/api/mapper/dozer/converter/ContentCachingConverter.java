package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

public class ContentCachingConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof ContentCaching && destinationClass.equals(Boolean.class)) {
            if (((ContentCaching)sourceFieldValue).isEnabled() == null) {
                return false;
            }
            return ((ContentCaching)sourceFieldValue).isEnabled();
        }

        if (sourceFieldValue instanceof Boolean) {
            ContentCaching conLog = new ContentCaching();
            conLog.setEnabled((Boolean) sourceFieldValue);
            return conLog;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}