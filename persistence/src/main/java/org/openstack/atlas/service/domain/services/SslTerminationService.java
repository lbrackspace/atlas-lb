package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import java.util.Calendar;
import java.util.List;

public interface SslTerminationService {

    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException;

    public boolean deleteSslTermination(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void pseudoDeleteSslTermination(Integer loadBalancerid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException;
}
