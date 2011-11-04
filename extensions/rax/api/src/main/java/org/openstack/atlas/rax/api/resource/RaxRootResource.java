package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.RootResource;

import javax.ws.rs.Path;

@Path("{accountId: [-+]?[0-9][0-9]*}")
public class RaxRootResource extends RootResource {

}
