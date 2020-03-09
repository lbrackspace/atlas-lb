package org.openstack.atlas.api.integration.threads;

import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.service.domain.entities.Host;

public class HostEndpointPollThread extends Thread {

    private ReverseProxyLoadBalancerService proxyService;
    private ReverseProxyLoadBalancerVTMService vtmProxyService;
    private Host host;
    private boolean endPointWorking = false;
    private boolean restEndPointWorking = false;
    private Exception exception = null;

    @Override
    public void run() {
        try {
            endPointWorking = proxyService.isEndPointWorking(host);
            restEndPointWorking = vtmProxyService.isEndPointWorking(host);
            return;
        } catch (Exception ex) {
            exception = ex;
            return;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HostEndpointPollThread{ ");
        if (host == null) {
            sb.append("null");
        } else {
            sb.append("host_id=").append(host.getId());
            sb.append(", url=").append(host.getEndpoint().toString());
        }
        sb.append("}");
        return sb.toString();
    }

    public ReverseProxyLoadBalancerService getProxyService() {
        return proxyService;
    }

    public void setProxyService(ReverseProxyLoadBalancerService proxyService) {
        this.proxyService = proxyService;
    }

    public ReverseProxyLoadBalancerVTMService getVTMProxyService() {
        return vtmProxyService;
    }

    public void setVTMProxyService(ReverseProxyLoadBalancerVTMService vtmProxyService) {
        this.vtmProxyService = vtmProxyService;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public boolean isEndPointWorking() {
        return endPointWorking;
    }

    public void setEndPointWorking(boolean endPointWorking) {
        this.endPointWorking = endPointWorking;
    }

    public boolean isRestEndPointWorking() {
        return restEndPointWorking;
    }

    public void setRestEndPointWorking(boolean restEndPointWorking) {
        this.restEndPointWorking = restEndPointWorking;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
