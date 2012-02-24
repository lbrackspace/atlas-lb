package org.openstack.atlas.service.domain.services;

import java.util.Set;

public interface AllowedDomainsService {

    public Set<String> getAllowedDomains();

    public boolean add(String name);

    public boolean remove(String name);

    public Set<String> matches(String name);

    public boolean hasHost(String name);

}
