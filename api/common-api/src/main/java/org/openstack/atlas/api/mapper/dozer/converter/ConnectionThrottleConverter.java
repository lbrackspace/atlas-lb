package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.dozer.CustomConverter;
import org.openstack.atlas.util.constants.ConnectionThrottleDefaultContants;
import org.openstack.atlas.service.domain.exceptions.NoMappableConstantException;

public class ConnectionThrottleConverter implements CustomConverter {

    @Override
    public Object convert(Object dstObj, Object srcObj, Class dstClass, Class srcClass) {
        ConnectionThrottle ct = null;
        ConnectionLimit cl = null;
        if (srcObj instanceof ConnectionThrottle && dstClass == ConnectionLimit.class) {
            ct = (ConnectionThrottle) srcObj;
            cl = new ConnectionLimit();


            if (ct.getMaxConnectionRate() == null) {
                cl.setMaxConnectionRate(ConnectionThrottleDefaultContants.getMaxConnectionRate());
            } else {
                cl.setMaxConnectionRate(ct.getMaxConnectionRate());
            }

            if (ct.getMinConnections() == null) {
                cl.setMinConnections(ConnectionThrottleDefaultContants.getMinConnections());
            } else {
                cl.setMinConnections(ct.getMinConnections());
            }

            cl.setMaxConnections(ct.getMaxConnections());

            if (ct.getRateInterval() == null) {
                cl.setRateInterval(ConnectionThrottleDefaultContants.getRateInterval());
            } else {
                cl.setRateInterval(ct.getRateInterval());
            }
            return cl;
        }
        if (srcObj instanceof ConnectionLimit && dstClass == ConnectionThrottle.class) {
            cl = (ConnectionLimit) srcObj;
            ct = new ConnectionThrottle();
            ct.setMaxConnectionRate(cl.getMaxConnectionRate());
            ct.setMinConnections(cl.getMinConnections());
            ct.setMaxConnections(cl.getMaxConnections());
            ct.setRateInterval(cl.getRateInterval());
            return ct;
        }
        throw new NoMappableConstantException("Cannot map source type: " + srcClass.getName());
    }
}
