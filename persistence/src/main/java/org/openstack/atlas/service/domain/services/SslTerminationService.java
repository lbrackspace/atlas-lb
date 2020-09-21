package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SslTerminationService {

    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination, boolean isSync) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException;

    public void validatePrivateKey(int lbId, int accountId, org.openstack.atlas.service.domain.entities.SslTermination sslTermination, boolean saveKey) throws BadRequestException, EntityNotFoundException;

    public boolean deleteSslTermination(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void pseudoDeleteSslTermination(Integer loadBalancerid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException;

    public Map<Integer, org.openstack.atlas.service.domain.entities.SslTermination> getAllMappedByLbId();
}
