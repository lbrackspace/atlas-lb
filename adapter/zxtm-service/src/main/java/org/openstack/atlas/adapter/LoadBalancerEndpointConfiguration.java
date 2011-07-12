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
    private String trafficManagerName;
    private List<String> failoverTrafficManagerNames;
    private Host trafficManagerHost;
    private String logFileLocation;

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host trafficManagerHost, List<String> failoverTrafficManagerNames) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        this.username = username;
        this.password = password;
        this.trafficManagerHost = trafficManagerHost;
        this.trafficManagerName = trafficManagerHost.getTrafficManagerName();
        this.failoverTrafficManagerNames = failoverTrafficManagerNames;
    }

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host trafficManagerHost, List<String> failoverTrafficManagerNames, String logFileLocation) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        this.username = username;
        this.password = password;
        this.trafficManagerHost = trafficManagerHost;
        this.trafficManagerName = trafficManagerHost.getTrafficManagerName();
        this.failoverTrafficManagerNames = failoverTrafficManagerNames;
        this.logFileLocation = logFileLocation;
    }

    public Host getTrafficManagerHost() {
        return trafficManagerHost;
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

    public String getTrafficManagerName() {
        return trafficManagerName;
    }

    public List<String> getFailoverTrafficManagerNames() {
        return failoverTrafficManagerNames;
    }

    public String getLogFileLocation() {
        return logFileLocation;
    }

    public void setLogFileLocation(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }
}
