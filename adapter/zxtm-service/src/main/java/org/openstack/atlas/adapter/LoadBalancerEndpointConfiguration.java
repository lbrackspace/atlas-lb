package org.openstack.atlas.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The LoadBalancerEndpointConfiguration class is used to pass the endpoint and
 * authentication details to the target adapter to establish connectivity to the
 * native load balancer.
 */
public class LoadBalancerEndpointConfiguration {

    public static Log LOG = LogFactory.getLog(LoadBalancerEndpointConfiguration.class);
    private URL endpointUrl;
    private URI restEndpoint;
    private String username;
    private String password;
    private String trafficManagerName;
    private List<String> failoverTrafficManagerNames;
    private Host trafficManagerHost;
    private Host endpointUrlHost;
    private String logFileLocation;
    private List<URI> restStatsEndpoints;
    private List<URI> soapStatsEndpoints;

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host trafficManagerHost, List<String> failoverTrafficManagerNames) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        try {
            this.restEndpoint = new URI(soapEndpoint.getRestEndpoint());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid rest endpoint...", e);
        }
        this.endpointUrlHost = soapEndpoint;
        this.username = username;
        this.password = password;
        this.trafficManagerHost = trafficManagerHost;
        this.trafficManagerName = trafficManagerHost.getTrafficManagerName();
        this.failoverTrafficManagerNames = failoverTrafficManagerNames;
        buildRestStatsEndpoints(soapEndpoint.getRestEndpoint(), this.trafficManagerName, this.failoverTrafficManagerNames);
        buildSoapStatsEndpoints(soapEndpoint.getEndpoint(), this.failoverTrafficManagerNames);
        LOG.info(String.format("Selecting %s as SoapEndpoint", this.endpointUrl));
    }

    public LoadBalancerEndpointConfiguration(Host soapEndpoint, String username, String password, Host trafficManagerHost, List<String> failoverTrafficManagerNames, String logFileLocation) {
        try {
            this.endpointUrl = new URL(soapEndpoint.getEndpoint());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid endpoint...", e);
        }
        try {
            this.restEndpoint = new URI(soapEndpoint.getRestEndpoint());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid rest endpoint...", e);
        }
        this.endpointUrlHost = soapEndpoint;
        this.username = username;
        this.password = password;
        this.trafficManagerHost = trafficManagerHost;
        this.trafficManagerName = trafficManagerHost.getTrafficManagerName();
        this.failoverTrafficManagerNames = failoverTrafficManagerNames;
        this.logFileLocation = logFileLocation;
        buildRestStatsEndpoints(soapEndpoint.getRestEndpoint(), this.trafficManagerName, this.failoverTrafficManagerNames);
        buildSoapStatsEndpoints(soapEndpoint.getEndpoint(), this.failoverTrafficManagerNames);
        LOG.info(String.format("Selecting %s as SoapEndPoint", this.endpointUrl));
        LOG.info(String.format("Selecting %s as RestEndPoint", this.restEndpoint));
    }

    @Override
    public String toString() {
        return "{"
                + " endpointUrlHost: " + endpointUrlHost
                + ", restEndpoint: " + restEndpoint
                + ", userName: " + username
                + ", passwd: " + "Censored"
                + ", trafficManagerHost: {" + ((trafficManagerHost == null) ? "null" : trafficManagerHost.toString()) + "}"
                + ", failoverTrafficManagerNames: " + StaticStringUtils.collectionToString(failoverTrafficManagerNames, ",")
                + "}";

    }

    private void buildRestStatsEndpoints(String restEndpoint, String trafficManagerHostName, List<String> failoverTrafficManagerNames) {
        restStatsEndpoints = new ArrayList<URI>();
        try {
            restStatsEndpoints.add(new URI(restEndpoint.substring(0, restEndpoint.indexOf("/config")) + "/status/" + trafficManagerHostName + "/statistics"));
            for (String string : failoverTrafficManagerNames) {
                if (!restEndpoint.contains("/config")) {
                    LOG.error(String.format("Endpoint %s did not contain necessary string to build stats endpoint.", restEndpoint));
                } else {
                    restStatsEndpoints.add(new URI(restEndpoint.substring(0, restEndpoint.indexOf("/config")) + "/status/" + string + "/statistics"));
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void buildSoapStatsEndpoints(String soapEndpoint, List<String> failoverTrafficManagerNames) {
        soapStatsEndpoints = new ArrayList<URI>();
        try {
            soapStatsEndpoints.add(new URI(soapEndpoint));
            for (String string : failoverTrafficManagerNames) {
                soapStatsEndpoints.add(new URI("https://" + string + ":9090/soap"));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Host getTrafficManagerHost() {
        return trafficManagerHost;
    }

    public URL getEndpointUrl() {
        return endpointUrl;
    }

    public URI getRestEndpoint() {
        return restEndpoint;
    }

    public void setRestEndpoint(URI restEndpoint) {
        this.restEndpoint = restEndpoint;
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

    public Host getEndpointUrlHost() {
        return endpointUrlHost;
    }

    public void setEndpointUrlHost(Host endpointUrlHost) {
        this.endpointUrlHost = endpointUrlHost;
    }

    public List<URI> getRestStatsEndpoints() { return restStatsEndpoints; }

    public void setRestStatsEndpoints(List<URI> endpoints) { this.restStatsEndpoints = endpoints; }
}
