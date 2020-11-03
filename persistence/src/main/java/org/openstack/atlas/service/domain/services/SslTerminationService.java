package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.util.Map;

public interface SslTerminationService {

    public ZeusSslTermination updateSslTermination(int lbId, int accountId, SslTermination sslTermination, boolean isSync) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException, InternalProcessingException;

    public ZeusCrtFile validatePrivateKey(int lbId, int accountId, org.openstack.atlas.service.domain.entities.SslTermination sslTermination, boolean saveKey) throws BadRequestException, EntityNotFoundException, UnprocessableEntityException, InternalProcessingException;

    public String decryptPrivateKey(int lbId, int accountId, String keyToDecrypt, boolean saveAlert) throws InternalProcessingException;

    public boolean deleteSslTermination(Integer loadBalancerId, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void pseudoDeleteSslTermination(Integer loadBalancerid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public org.openstack.atlas.service.domain.entities.SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException;

    public Map<Integer, org.openstack.atlas.service.domain.entities.SslTermination> getAllMappedByLbId();
}
