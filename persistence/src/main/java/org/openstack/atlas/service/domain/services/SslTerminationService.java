package org.openstack.atlas.service.domain.services;

import javassist.tools.rmi.ObjectNotFoundException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;

import java.util.Calendar;
import java.util.List;

public interface SslTerminationService {

    public SslTermination setSslTermination(Integer lid, Integer accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException;

    boolean updateSslTermination(int id, Integer accountId, SslTermination domainSslTermination) throws EntityNotFoundException;

    public boolean deleteSslTermination(int id, Integer accountId, SslTermination domainSslTermination) throws EntityNotFoundException;

    public SslTermination getSslTermination(int id, Integer accountId) throws EntityNotFoundException;
}
