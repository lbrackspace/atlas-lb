package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class NodesResource extends ManagementDependencyProvider {

    private NodeResource nodeResource;

    @Path("{id: [1-9][0-9]*}")
    public NodeResource getHostResource(@PathParam("id") int id) {
        nodeResource.setId(id);
        return nodeResource;
    }


    public NodeResource getNodeResource() { return nodeResource; }

    public void setNodeResource(NodeResource nodeResource) {
        this.nodeResource = nodeResource;
    }


}
