package org.openstack.atlas.adapter;

import org.openstack.atlas.service.domain.entities.Host;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * The LoadBalancerEndpointConfiguration class is used to pass the endpoint and
 * authentication details to the target adapter to establish connectivity to the
 * native load balancer.
 */

public class LoadBalancerEndpointConfiguration {
    private URL endpointUrl;
    private String username;
    private String password;
    private String hostName;
    private List<String> failoverHostNames;
    private Host host;
    private String logFileLocation;

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host host, List<String> failoverHostNames) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        this.username = username;
        this.password = password;
        this.host = host;
        this.hostName = host.getHostName();
        this.failoverHostNames = failoverHostNames;
    }

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host host, List<String> failoverHostNames, String logFileLocation) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        this.username = username;
        this.password = password;
        this.host = host;
        this.hostName = host.getHostName();
        this.failoverHostNames = failoverHostNames;
        this.logFileLocation = logFileLocation;
    }

    public Host getHost() {
        return host;
    }

    public URL getEndpointUrl() {
        return endpointUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostName() {
        return hostName;
    }

    public List<String> getFailoverHostNames() {
        return failoverHostNames;
    }

    public String getLogFileLocation() {
        return logFileLocation;
    }

    public void setLogFileLocation(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }
}
