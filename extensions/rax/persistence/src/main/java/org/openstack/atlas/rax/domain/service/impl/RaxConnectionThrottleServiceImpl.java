package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.rax.domain.entity.RaxConnectionThrottle;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.service.impl.ConnectionThrottleServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RaxConnectionThrottleServiceImpl extends ConnectionThrottleServiceImpl {

    @Override
    protected void setPropertiesForUpdate(final ConnectionThrottle requestConnectionThrottle, final ConnectionThrottle dbConnectionThrottle) throws BadRequestException {
        super.setPropertiesForUpdate(requestConnectionThrottle, dbConnectionThrottle);

        if (requestConnectionThrottle instanceof RaxConnectionThrottle) {
            RaxConnectionThrottle raxConnectionThrottle = (RaxConnectionThrottle) requestConnectionThrottle;
            RaxConnectionThrottle raxDbConnectionThrottle = (RaxConnectionThrottle) dbConnectionThrottle;
            if(raxConnectionThrottle.getMinConnections() != null) {
                raxDbConnectionThrottle.setMinConnections(raxConnectionThrottle.getMinConnections());
            }
            if(raxConnectionThrottle.getMaxConnections() != null) {
                raxDbConnectionThrottle.setMaxConnections(raxConnectionThrottle.getMaxConnections());
            }
        }
    }
}
