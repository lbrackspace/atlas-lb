package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.pojos.LBDeviceEvent;

public interface CallbackService {
    public void handleLBDeviceEvent(LBDeviceEvent map) throws BadRequestException;
}
