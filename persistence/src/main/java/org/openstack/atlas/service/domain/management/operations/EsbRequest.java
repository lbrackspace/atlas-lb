package org.openstack.atlas.service.domain.management.operations;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.AccountGroup;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.entities.Suspension;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Sync;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlocks;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;

import java.io.Serializable;
import java.util.List;

public class EsbRequest implements Serializable {

    private final static long serialVersionUID = 532512316L;
    private VirtualIpBlocks virtualIpBlocks;
    private List<VirtualIp> virtualIps;
    private LoadBalancer loadBalancer;
    private org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer mLoadBalancer;
    private List<LoadBalancer> loadBalancers;
    private Cluster cluster;
    private List<Host> hosts;
    private Host host;
    private Suspension suspension;
    private ZeusEvent zeusEvent;
    private Sync syncObject;
    private Alert alert;
    private AccountGroup accountGroup;
    private List<AccountGroup> accountGroups;
    private GroupRateLimit groupRateLimit;
    private Hostssubnet hostssubnet;
    private int zeusHostConcurrentConnections;
    private RateLimit rateLimit;
    private List<BlacklistItem> blacklistItems;
    private BlacklistItem blacklistItem;

    public void setBlacklistItems(List<BlacklistItem> blacklistItems) {
        this.blacklistItems = blacklistItems;
    }

    public List<BlacklistItem> getBlacklistItems() {
        return blacklistItems;
    }

    public BlacklistItem getBlacklistItem() {
        return blacklistItem;
    }

    public void setBlacklistItem(BlacklistItem blacklistItem) {
        this.blacklistItem = blacklistItem;
    }

    private LoadBalancerStatus loadbalancerStatustoCheck;

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public List<VirtualIp> getVirtualIps() {
        return virtualIps;
    }

    public void setVirtualIps(List<VirtualIp> virtualIps) {
        this.virtualIps = virtualIps;
    }

    public ZeusEvent getZeusEvent() {
        return zeusEvent;
    }

    public void setZeusEvent(ZeusEvent zeusEvent) {
        this.zeusEvent = zeusEvent;
    }

    public Suspension getSuspension() {
        return suspension;
    }

    public void setSuspension(Suspension suspension) {
        this.suspension = suspension;
    }

    public List<LoadBalancer> getLoadBalancers() {
        return loadBalancers;
    }

    public void setLoadBalancers(List<LoadBalancer> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public Host getHost() {
        if (host == null) {
            host = new Host();
        }
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public VirtualIpBlocks getVirtualIpBlocks() {
        if (virtualIpBlocks == null) {
            virtualIpBlocks = new VirtualIpBlocks();
        }
        return virtualIpBlocks;
    }

    public void setVirtualIpBlocks(VirtualIpBlocks virtualIpBlocks) {
        this.virtualIpBlocks = virtualIpBlocks;
    }

    public LoadBalancer getLoadBalancer() {
        if (loadBalancer == null) {
            loadBalancer = new LoadBalancer();
        }
        return loadBalancer;
    }

    public org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer getMgmtLoadBalancer() {
        if (mLoadBalancer == null) {
            mLoadBalancer = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
        }
        return mLoadBalancer;
    }

    public void setMgmtLoadBalancer(org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer mLoadBalancer) {
        this.mLoadBalancer = mLoadBalancer;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Cluster getCluster() {
        if (cluster == null) {
            cluster = new Cluster();
        }
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void setZeusHostConcurrentConnections(int zeusHostConcurrentConnections) {
        this.zeusHostConcurrentConnections = zeusHostConcurrentConnections;
    }

    public int getZeusHostConcurrentConnections() {
        return zeusHostConcurrentConnections;
    }

    public Sync getSyncObject() {
        return syncObject;
    }

    public GroupRateLimit getGroupRateLimit() {
        return groupRateLimit;
    }

    public void setGroupRateLimit(GroupRateLimit groupRateLimit) {
        this.groupRateLimit = groupRateLimit;
    }

    public void setSyncObject(Sync syncObject) {
        this.syncObject = syncObject;
    }

    public Hostssubnet getHostssubnet() {
        return hostssubnet;
    }

    public void setHostssubnet(Hostssubnet hostssubnet) {
        this.hostssubnet = hostssubnet;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<AccountGroup> getAccountGroups() {
        return accountGroups;
    }

    public void setAccountGroups(List<AccountGroup> accountGroups) {
        this.accountGroups = accountGroups;
    }

    public LoadBalancerStatus getLoadbalancerStatustoCheck() {
        return loadbalancerStatustoCheck;
    }

    public void setLoadbalancerStatustoCheck(LoadBalancerStatus loadbalancerStatustoCheck) {
        this.loadbalancerStatustoCheck = loadbalancerStatustoCheck;
    }

    public AccountGroup getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(AccountGroup accountGroup) {
        this.accountGroup = accountGroup;
    }
}
