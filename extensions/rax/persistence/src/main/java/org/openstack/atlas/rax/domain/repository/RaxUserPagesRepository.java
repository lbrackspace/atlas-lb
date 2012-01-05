package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.rax.domain.entity.RaxDefaults;
import org.openstack.atlas.rax.domain.entity.RaxUserPages;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface RaxUserPagesRepository {
    RaxUserPages getUserPagesByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId);

    RaxDefaults getDefaultErrorPage() throws EntityNotFoundException;

    String getErrorPageByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;

    boolean setErrorPage(Integer accountId, Integer loadBalancerId, String errorpage) throws EntityNotFoundException;

    boolean setDefaultErrorPage(String errorpage) throws EntityNotFoundException;

    boolean deleteErrorPage(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException;
}
