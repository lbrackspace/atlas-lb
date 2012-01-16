package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.rax.domain.entity.RaxDefaults;
import org.openstack.atlas.rax.domain.entity.RaxUserPages;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;

public interface RaxUserPagesService {

    void setErrorPage(Integer accountId, Integer loadBalancerId, String errorpage) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;

    void setDefaultErrorPage(String errorpage) throws EntityNotFoundException;

    void deleteErrorPage(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException;
}
