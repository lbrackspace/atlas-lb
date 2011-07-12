package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.Cluster;

import java.util.List;

public class ClusterDetails {

    /*  * ID
* Name
* Description
* DC Location
* # / Active Load Balancing Configurations
* # / Unique Customers
* # / Host Machines
* Average utilization of all hosts (excluding hosts with FAILOVER status)   */


    private Cluster cluster;
    private Integer numberOfActiveLoadBalancer;
    private Integer noOfuniqueCustomer;
    private Integer numberOfHostMachines;
    private String averageUtilizationofHosts;
    private List<HostMachineDetails> hostMachineDetails;
    private CapacityPlanningVirtualIpBlocks virtualIpBlocks;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Integer getNumberOfActiveLoadBalancer() {
        return numberOfActiveLoadBalancer;
    }

    public void setNumberOfActiveLoadBalancer(Integer numberOfActiveLoadBalancer) {
        this.numberOfActiveLoadBalancer = numberOfActiveLoadBalancer;
    }

    public Integer getNoOfuniqueCustomer() {
        return noOfuniqueCustomer;
    }

    public void setNoOfuniqueCustomer(Integer noOfuniqueCustomer) {
        this.noOfuniqueCustomer = noOfuniqueCustomer;
    }

    public Integer getNumberOfHostMachines() {
        return numberOfHostMachines;
    }

    public void setNumberOfHostMachines(Integer numberOfHostMachines) {
        this.numberOfHostMachines = numberOfHostMachines;
    }

    public String getAverageUtilizationofHosts() {
        return averageUtilizationofHosts;
    }

    public void setAverageUtilizationofHosts(String averageUtilizationofHosts) {
        this.averageUtilizationofHosts = averageUtilizationofHosts;
    }

    public List<HostMachineDetails> getHostMachineDetails() {
        return hostMachineDetails;
    }


    public void setHostMachineDetails(List<HostMachineDetails> hostMachineDetails) {
        this.hostMachineDetails = hostMachineDetails;
    }    

    public CapacityPlanningVirtualIpBlocks getVirtualIpBlocks() {
        return virtualIpBlocks;
    }

    public void setVirtualIpBlocks(CapacityPlanningVirtualIpBlocks virtualIpBlocks) {
        this.virtualIpBlocks = virtualIpBlocks;
    }


}
