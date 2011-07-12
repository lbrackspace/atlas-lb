package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;

public interface CallbackService {
    public void handleZeusEvent(ZeusEvent map) throws BadRequestException;
}
