package org.openstack.atlas.service.domain.pojos;

import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ExtendedAccountLoadBalancer implements Serializable {
    private int loadBalancerId;
    private String loadBalancerName;
    private int clusterId;
    private String clusterName;
    private DataCenter region;
    private String status;
    private String protocol;
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
    private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
    private VirtualIpDozerWrapper virtualIpDozerWrapper;

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public DataCenter getRegion() {
        return region;
    }

    public void setRegion(DataCenter region) {
        this.region = region;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String clusterStatus) {
        this.status = clusterStatus;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

   public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        if (loadBalancerJoinVipSet == null) {
            loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
        }
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }

    public Set<LoadBalancerJoinVip6> getLoadBalancerJoinVip6Set() {
        if (loadBalancerJoinVip6Set == null) {
            loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
        }
        return loadBalancerJoinVip6Set;
    }

    public void setLoadBalancerJoinVip6Set(Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set) {
        this.loadBalancerJoinVip6Set = loadBalancerJoinVip6Set;
    }

    public VirtualIpDozerWrapper getVirtualIpDozerWrapper() {
        return new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);
    }

    public void setVirtualIpDozerWrapper(VirtualIpDozerWrapper virtualIpDozerWrapper) {
        this.virtualIpDozerWrapper = virtualIpDozerWrapper;
        this.setLoadBalancerJoinVipSet(this.virtualIpDozerWrapper.getLoadBalancerJoinVipSet());
        this.setLoadBalancerJoinVip6Set(this.virtualIpDozerWrapper.getLoadBalancerJoinVip6Set());
    }
}
