package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;

import java.util.List;

public interface CertificateMappingService {

    CertificateMapping create(LoadBalancer lb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException;

    List<CertificateMapping> getAllForLoadBalancerId(Integer lbId) throws EntityNotFoundException;

    CertificateMapping getByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException;

    void update(LoadBalancer lb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException;

    void prepareForDelete(Integer id, Integer loadBalancerId);

    void deleteByIdAndLoadBalancerId(Integer id, Integer lbId) throws EntityNotFoundException;
}
