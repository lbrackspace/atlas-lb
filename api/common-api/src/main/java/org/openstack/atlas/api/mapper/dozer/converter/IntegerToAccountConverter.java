package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Account;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.dozer.CustomConverter;

public class IntegerToAccountConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Integer) {
            Account account = new Account();
            account.setId((Integer) sourceFieldValue);
            return account;
        }

        if (sourceFieldValue instanceof Account) {
            return ((Account) sourceFieldValue).getId();
        }
        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
