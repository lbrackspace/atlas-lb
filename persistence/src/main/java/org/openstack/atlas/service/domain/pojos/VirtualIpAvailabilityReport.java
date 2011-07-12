package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;


public class VirtualIpAvailabilityReport
    implements Serializable
{

    private final static long serialVersionUID = 532512316L;
    protected Integer clusterId;
    protected String clusterName;
    protected Long totalPublicIpAddresses;
    protected Long totalServiceNetAddresses;
    protected Long freeAndClearPublicIpAddresses;
    protected Long freeAndClearServiceNetIpAddresses;
    protected Long publicIpAddressesInHolding;
    protected Long serviceNetIpAddressesInHolding;
    protected Long publicIpAddressesAllocatedToday;
    protected Long serviceNetIpAddressesAllocatedToday;
    protected Long allocatedPublicIpAddressesInLastSevenDays;
    protected Long allocatedServiceNetIpAddressesInLastSevenDays;
    protected Double remainingDaysOfPublicIpAddresses;
    protected Double remainingDaysOfServiceNetIpAddresses;

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer value) {
        this.clusterId = value;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String value) {
        this.clusterName = value;
    }

    public Long getTotalPublicIpAddresses() {
        return totalPublicIpAddresses;
    }

    public void setTotalPublicIpAddresses(Long value) {
        this.totalPublicIpAddresses = value;
    }

    public Long getTotalServiceNetAddresses() {
        return totalServiceNetAddresses;
    }

    public void setTotalServiceNetAddresses(Long value) {
        this.totalServiceNetAddresses = value;
    }

    public Long getFreeAndClearPublicIpAddresses() {
        return freeAndClearPublicIpAddresses;
    }

    public void setFreeAndClearPublicIpAddresses(Long value) {
        this.freeAndClearPublicIpAddresses = value;
    }

    public Long getFreeAndClearServiceNetIpAddresses() {
        return freeAndClearServiceNetIpAddresses;
    }

    public void setFreeAndClearServiceNetIpAddresses(Long value) {
        this.freeAndClearServiceNetIpAddresses = value;
    }

    public Long getPublicIpAddressesInHolding() {
        return publicIpAddressesInHolding;
    }

    public void setPublicIpAddressesInHolding(Long value) {
        this.publicIpAddressesInHolding = value;
    }

    public Long getServiceNetIpAddressesInHolding() {
        return serviceNetIpAddressesInHolding;
    }

    public void setServiceNetIpAddressesInHolding(Long value) {
        this.serviceNetIpAddressesInHolding = value;
    }

    public Long getPublicIpAddressesAllocatedToday() {
        return publicIpAddressesAllocatedToday;
    }

    public void setPublicIpAddressesAllocatedToday(Long value) {
        this.publicIpAddressesAllocatedToday = value;
    }

    public Long getServiceNetIpAddressesAllocatedToday() {
        return serviceNetIpAddressesAllocatedToday;
    }

    public void setServiceNetIpAddressesAllocatedToday(Long value) {
        this.serviceNetIpAddressesAllocatedToday = value;
    }

    public Long getAllocatedPublicIpAddressesInLastSevenDays() {
        return allocatedPublicIpAddressesInLastSevenDays;
    }

    public void setAllocatedPublicIpAddressesInLastSevenDays(Long value) {
        this.allocatedPublicIpAddressesInLastSevenDays = value;
    }

    public Long getAllocatedServiceNetIpAddressesInLastSevenDays() {
        return allocatedServiceNetIpAddressesInLastSevenDays;
    }

    public void setAllocatedServiceNetIpAddressesInLastSevenDays(Long value) {
        this.allocatedServiceNetIpAddressesInLastSevenDays = value;
    }

    public Double getRemainingDaysOfPublicIpAddresses() {
        return remainingDaysOfPublicIpAddresses;
    }

    public void setRemainingDaysOfPublicIpAddresses(Double value) {
        this.remainingDaysOfPublicIpAddresses = value;
    }

    public Double getRemainingDaysOfServiceNetIpAddresses() {
        return remainingDaysOfServiceNetIpAddresses;
    }

    public void setRemainingDaysOfServiceNetIpAddresses(Double value) {
        this.remainingDaysOfServiceNetIpAddresses = value;
    }

}
