package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.Usage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancerBilling implements Serializable {
    private final static long serialVersionUID = 532512316L;
    private Integer loadBalancerId;
    private String loadBalancerName;
    private List<Usage> usageRecords = new ArrayList<Usage>();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadbalancerName) {
        this.loadBalancerName = loadbalancerName;
    }

    public List<Usage> getUsageRecords() {
        return usageRecords;
    }

    public void setUsageRecords(List<Usage> historicalUsage) {
        this.usageRecords = historicalUsage;
    }
}
