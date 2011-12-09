package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.rax.domain.entity.RaxHealthMonitor;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.service.impl.HealthMonitorServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RaxHealthMonitorServiceImpl extends HealthMonitorServiceImpl {

    @Override
    protected void setPropertiesForUpdate(final HealthMonitor healthMonitor, final HealthMonitor dbHealthMonitor, HealthMonitor healthMonitorToUpdate) throws BadRequestException {
        super.setPropertiesForUpdate(healthMonitor, dbHealthMonitor, healthMonitorToUpdate);

        if (healthMonitorToUpdate instanceof RaxHealthMonitor) {
            RaxHealthMonitor raxHealthMonitorToUpdate = (RaxHealthMonitor) healthMonitorToUpdate;
            RaxHealthMonitor raxHealthMonitor = (RaxHealthMonitor) healthMonitor;
            if (raxHealthMonitor.getStatusRegex() != null) {
                raxHealthMonitorToUpdate.setStatusRegex(raxHealthMonitor.getStatusRegex());
            }
            if (raxHealthMonitor.getBodyRegex() != null) {
                raxHealthMonitorToUpdate.setBodyRegex(raxHealthMonitor.getBodyRegex());
            }
        }
    }
}
