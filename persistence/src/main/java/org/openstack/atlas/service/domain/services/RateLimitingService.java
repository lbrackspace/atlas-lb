package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;

import java.util.List;

public interface RateLimitingService {
    public RateLimit get(Integer id) throws EntityNotFoundException, DeletedStatusException;

    public List<RateLimit> retrieveLoadBalancerRateLimits();

    public void create(LoadBalancer dblb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException;

    public void update(LoadBalancer dbLb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException;

    public void delete(LoadBalancer dbLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException;

    public void pseudoDelete(LoadBalancer dbLb) throws EntityNotFoundException;

    public void removeLimitByExpiration(int id);

}
