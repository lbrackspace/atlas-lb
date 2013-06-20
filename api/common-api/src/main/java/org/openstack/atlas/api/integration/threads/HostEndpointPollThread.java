package org.openstack.atlas.api.integration.threads;

import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.Host;

public class HostEndpointPollThread extends Thread {

    private ReverseProxyLoadBalancerService proxyService;
    private Host host;
    private boolean endPointWorking = false;
    private Exception exception = null;

    @Override
    public void run() {
        try {
            endPointWorking = proxyService.isEndPointWorking(host);
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

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
