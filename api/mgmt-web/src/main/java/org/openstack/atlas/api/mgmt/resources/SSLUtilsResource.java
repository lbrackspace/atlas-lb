package org.openstack.atlas.api.mgmt.resources;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfStrings;


// SSL Tools for testing SSL crust.
public class SSLUtilsResource extends ManagementDependencyProvider{

    @GET
    @Path("roots")
    public Response getRoots(){
        ListOfStrings buildCas = new ListOfStrings();
        return Response.status(Response.Status.OK).entity(buildCas).build();
    }
}
