package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.*;

import java.util.List;

public interface CertificateMappingService {

    CertificateMapping create(LoadBalancer lb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException, LimitReachedException, InternalProcessingException;

    List<CertificateMapping> getAllForLoadBalancerId(Integer lbId) throws EntityNotFoundException;

    CertificateMapping getByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException;

    void deleteAllCertMappingForLB(Integer lbId) throws EntityNotFoundException;

    void update(LoadBalancer lb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException, InternalProcessingException;

    void validatePrivateKeys(LoadBalancer messengerLb, boolean saveKeys) throws BadRequestException, UnprocessableEntityException, InternalProcessingException;

    void prepareForDelete(LoadBalancer lb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    void deleteByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException;
}
