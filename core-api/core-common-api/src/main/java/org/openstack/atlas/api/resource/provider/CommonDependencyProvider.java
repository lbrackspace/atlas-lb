package org.openstack.atlas.api.resource.provider;

import org.dozer.DozerBeanMapper;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.exceptions.BadRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CommonDependencyProvider {
    protected final static String NOBODY = "Undefined User";
    protected final static String USERHEADERNAME = "X-PP-User";
    protected final static String VFAIL = "Validation Failure";

    @Autowired
    protected DozerBeanMapper dozerMapper;
    @Autowired
    protected AsyncService asyncService;

    public String getUserName(HttpHeaders headers){
        if(headers == null || headers.getRequestHeader(USERHEADERNAME).size()<1){
            return NOBODY;
        }
        String userName = headers.getRequestHeader(USERHEADERNAME).get(0);
        if(userName == null){
            return NOBODY;
        }
        return userName;
    }

     public Response getValidationFaultResponse(ValidatorResult result) {
        List<String> vmessages = result.getValidationErrorMessages();
        int status = 400;
        BadRequest badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, vmessages);
        Response vresp = Response.status(status).entity(badreq).build();
        return vresp;
    }

    public Response getValidationFaultResponse(List<String> errorStrs) {
        BadRequest badreq;
        int status = 400;
        badreq = HttpResponseBuilder.buildBadRequestResponse(VFAIL, errorStrs);
        Response resp = Response.status(status).entity(badreq).build();
        return resp;
    }
}
