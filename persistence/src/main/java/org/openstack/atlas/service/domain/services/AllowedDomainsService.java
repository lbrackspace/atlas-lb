package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.AllowedDomain;

import java.util.List;

public interface AllowedDomainsService {

    public List<AllowedDomain> getAllowedDomains();

    public AllowedDomain getAllowedDomainById(int id);

    public void createAllowedDomain(List<AllowedDomain> allowedDomain);

    public void deleteAllowedDomain(AllowedDomain allowedDomainId);

}
