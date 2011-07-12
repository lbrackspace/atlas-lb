package org.openstack.atlas.api.mgmt.helpers;

import org.openstack.atlas.util.ip.IPUtils;
import javax.ws.rs.core.Response;

public class CheckQueryParams {
    public Response checkParams(String address, Integer id) {
        if (address != null && id != null) {
            return Response.status(400).entity("Cannot set both ip address and vip id").build();
        }
        if (address != null) {
            if(!IPUtils.isValidIpv4String(address) && !IPUtils.isValidIpv6String(address))
                return Response.status(400).entity("Ip address is invalid").build();

        }
        return null;
    }
}
