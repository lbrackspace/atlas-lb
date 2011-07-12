package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.Host;

public class HostMachineDetails {


    /* id
     Name
    * Zone (A / B)
    * Current Rated Utilization (%)
    * Maximum # of Rated Concurrent Connections
    * CORE Device ID#
    * Status (PROVISIONING_TARGET / ACTIVE / BURN-IN / OFFLINE)
    * Management IP Address
    * # / Active Load Balancing Configurations
    * # / Unique Customers
*/
    private Host host;
    private String currentUtilization;
    private long activeLBConfigurations;
    private Integer uniqueCustomers;
    private Integer totalConcurrentConnections;
    private Integer availableConcurrentConnections;

    public Host getHost() {
        return host;
    }
    public Integer getTotalConcurrentConnections() {
        return totalConcurrentConnections;
    }

    public Integer getAvailableConcurrentConnections() {
        return availableConcurrentConnections;
    }

    public void setAvailableConcurrentConnections(Integer availableConcurrentConnections) {
        this.availableConcurrentConnections = availableConcurrentConnections;
    }

    public void setTotalConcurrentConnections(Integer totalConcurrentConnections) {
        this.totalConcurrentConnections = totalConcurrentConnections;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public String getCurrentUtilization() {
        return currentUtilization;
    }

    public void setCurrentUtilization(String currentUtilization) {
        this.currentUtilization = currentUtilization;
    }

    public long getActiveLBConfigurations() {
        return activeLBConfigurations;
    }

    public void setActiveLBConfigurations(long activeLBConfigurations) {
        this.activeLBConfigurations = activeLBConfigurations;
    }

    public Integer getUniqueCustomers() {
        return uniqueCustomers;
    }

    public void setUniqueCustomers(Integer uniqueCustomers) {
        this.uniqueCustomers = uniqueCustomers;
    }

}




