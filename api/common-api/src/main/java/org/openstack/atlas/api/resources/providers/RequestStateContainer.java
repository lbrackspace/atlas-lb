package org.openstack.atlas.api.resources.providers;

// Class that is can be injected into filters and the BaseResource class so
import java.util.Calendar;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

// that you can pass information between the filter and Resource classes.
// Set the bean to scope="prototype" then set the property on any class that
// needs to share state at the Request level
public class RequestStateContainer {
    private static final double msco = 60.00/100.00; // Millisecond to second coefficient
    
    private HttpHeaders httpHeaders;
    private SecurityContext securityContext;
    private UriInfo uriInfo;
    private final String id;

    public RequestStateContainer() {
        this.id = getHexHash();
        nop();

    }

    public String getId(){
        return this.id;
    }


    public final String getHexHash() {
        String out;
        String hexHash = Integer.toHexString(System.identityHashCode(this));
        Calendar now = Calendar.getInstance();
        int hr = now.get(Calendar.HOUR);
        int mn = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);
        int ms = now.get(Calendar.MILLISECOND);
        int subsec = (int)(msco*ms);
        out = String.format("%d:%d:%d:%d : %s",hr,mn,sec,subsec,hexHash);
        return out;
    }

    private void nop() {
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
