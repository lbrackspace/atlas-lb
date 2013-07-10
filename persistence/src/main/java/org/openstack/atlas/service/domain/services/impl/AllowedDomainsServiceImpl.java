package org.openstack.atlas.service.domain.services.impl;

import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.AllowedDomain;
import org.openstack.atlas.service.domain.services.AllowedDomainsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import org.openstack.atlas.service.domain.repository.AllowedDomainsRepository;

@Service
public class AllowedDomainsServiceImpl extends BaseService implements AllowedDomainsService {
    private final Log LOG = LogFactory.getLog(AllowedDomainsServiceImpl.class);

    @Override
    public Set<String> getAllowedDomains() {
        return allowedDomainsRepository.getAllowedDomains();
    }

    @Override
    public boolean add(String name) {
        return allowedDomainsRepository.add(name);
    }

    @Override
    public boolean remove(String name) {
        return allowedDomainsRepository.remove(name);
    }

    @Override
    public Set<String> matches(String hostName){
        Set<String> domains = allowedDomainsRepository.getAllowedDomains();
        Set<String> matchedDomains = new HashSet<String>();
        for(String domain : domains){
            if(hostInDomain(hostName,domain)){
                matchedDomains.add(domain);
            }
        }
        return matchedDomains;
    }

    @Override
    public boolean hasHost(String hostName){
        Set<String> ads = matches(hostName);
        return ads.size() > 0;

    }


    public boolean hostInDomain(String host,String domain){
        String[] dcomp = domain.split("\\.");
        String[] hcomp = host.split("\\.");
        int di = dcomp.length - 1;
        int hi = hcomp.length - 1;
        int i;
        if(di>hi){
            return false;
        }
        while(di>=0){
            if(!dcomp[di].equals(hcomp[hi])){
                return false;
            }
            di--;
            hi--;
        }
        return true;
    }
}
