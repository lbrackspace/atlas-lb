package org.openstack.atlas.api.resource.provider;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CommonDependencyProvider {
    @Autowired
    protected DozerBeanMapper dozerMapper;
}
