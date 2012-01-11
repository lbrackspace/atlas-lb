package org.openstack.atlas.service.domain.services;

import javassist.tools.rmi.ObjectNotFoundException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import java.util.Calendar;
import java.util.List;

public interface SslTerminationService {

    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException;

    public boolean deleteSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    public SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException;
}
