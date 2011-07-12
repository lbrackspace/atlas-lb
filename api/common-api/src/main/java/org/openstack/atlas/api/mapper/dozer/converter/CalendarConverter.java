package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

import java.util.Calendar;

public class CalendarConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Calendar && destinationClass.equals(Created.class)) {
            Created created = new Created();
            created.setTime((Calendar) sourceFieldValue);
            return created;
        }

        if (sourceFieldValue instanceof Calendar && destinationClass.equals(Updated.class)) {
            Updated updated = new Updated();
            updated.setTime((Calendar) sourceFieldValue);
            return updated;
        }

        if (sourceFieldValue instanceof Created) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(((Created) sourceFieldValue).getTime().getTime());
            return cal;
        }

        if (sourceFieldValue instanceof Updated) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(((Updated) sourceFieldValue).getTime().getTime());
            return cal;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}