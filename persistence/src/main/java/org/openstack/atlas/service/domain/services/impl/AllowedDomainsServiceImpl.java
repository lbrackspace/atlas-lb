package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AllowedDomain;
import org.openstack.atlas.service.domain.services.AllowedDomainsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllowedDomainsServiceImpl extends BaseService implements AllowedDomainsService {

    private final Log LOG = LogFactory.getLog(AllowedDomainsServiceImpl.class);

    @Override
    public List<AllowedDomain> getAllowedDomains() {
        return allowedDomainsRepository.getAllAllowedDomains();
    }

    @Override
    public AllowedDomain getAllowedDomainById(int id) {
        return allowedDomainsRepository.getAllowedDomain(id);
    }

    @Override
    public void createAllowedDomain(List<AllowedDomain> allowedDomains) {
        allowedDomainsRepository.createAllowedDomains(allowedDomains);
    }

    @Override
    public void deleteAllowedDomain(AllowedDomain allowedDomain) {
        allowedDomainsRepository.delete(allowedDomain);
    }

    //TODO: method for other services to call for domain verification...
}
