package org.openstack.atlas.api.mapper.dozer.converter;

import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.dozer.CustomConverter;
import org.openstack.atlas.util.constants.ConnectionThrottleDefaultConstants;
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
                cl.setMaxConnectionRate(ConnectionThrottleDefaultConstants.getMaxConnectionRate());
            } else {
                cl.setMaxConnectionRate(ct.getMaxConnectionRate());
            }

            if (ct.getMinConnections() == null) {
                cl.setMinConnections(ConnectionThrottleDefaultConstants.getMinConnections());
            } else {
                cl.setMinConnections(ct.getMinConnections());
            }

            cl.setMaxConnections(ct.getMaxConnections());

            if (ct.getRateInterval() == null) {
                cl.setRateInterval(ConnectionThrottleDefaultConstants.getRateInterval());
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
