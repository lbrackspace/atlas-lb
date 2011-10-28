package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.AlgorithmsResource;
import org.openstack.atlas.api.resource.ProtocolsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("{accountId: [-+]?[0-9][0-9]*}")
public class RootResource extends org.openstack.atlas.api.resource.RootResource {

}
