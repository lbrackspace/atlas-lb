package org.openstack.atlas.api.mapper.dozer.converter;

import org.dozer.CustomConverter;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;
import org.openstack.atlas.service.domain.pojos.Stats;

import java.util.Map;

public class StatsConverter implements CustomConverter {

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        if (sourceFieldValue instanceof Map && destinationClass.equals(Stats.class)) {
//                Stats stats = new Stats();
//                stats.setBytesIn((String) ((Map) sourceFieldValue).get("bytesIn"));
//                stats.setBytesOut((String) ((Map) sourceFieldValue).get("bytesOut"));
//                stats.setConnectTimeOut((String) ((Map) sourceFieldValue).get("connectTimeOut"));
//                stats.setConnectError((String) ((Map) sourceFieldValue).get("connectError"));
//                stats.setConnectFailure((String) ((Map) sourceFieldValue).get("connectFailure"));
//                stats.setCurrentConn((String) ((Map) sourceFieldValue).get("currentConn"));
//                stats.setDataTimedOut((String) ((Map) sourceFieldValue).get("dataTimeOut"));
//                stats.setMaxConn((String) ((Map) sourceFieldValue).get("maxConn"));
            return null;
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}