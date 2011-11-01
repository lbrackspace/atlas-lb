package org.openstack.atlas.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.core.api.v1.Protocol;
import org.openstack.atlas.core.api.v1.Protocols;
import org.openstack.atlas.datamodel.ProtocolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@Controller
@Scope("request")
public class ProtocolsResource {

    @Autowired
    ProtocolType protocolType;

    @GET
    public Response retrieveLoadBalancingAlgorithms() {
        try {
            Protocols protocols = new Protocols();

            for (String protocolName : protocolType.toList()) {
                Protocol protocol = new Protocol();
                protocol.setName(protocolName);
                protocols.getProtocols().add(protocol);
            }

            return Response.status(Response.Status.OK).entity(protocols).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }
}
