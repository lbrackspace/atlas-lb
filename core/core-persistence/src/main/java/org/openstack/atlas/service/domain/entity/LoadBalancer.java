package org.openstack.atlas.service.domain.entity;

import org.hibernate.annotations.Cascade;
import org.openstack.atlas.service.domain.pojo.VirtualIpDozerWrapper;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Table(name = "load_balancer")
public class LoadBalancer extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;
    
    @Column(name = "name", length = 128)
    private String name;

    @OneToMany(mappedBy = "loadBalancer", fetch = FetchType.EAGER)
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

    @OneToMany(mappedBy = "loadBalancer", fetch = FetchType.EAGER)
    private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.EAGER)
    private Set<Node> nodes = new HashSet<Node>();

    @OrderBy("id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.LAZY)
    private Set<UsageRecord> usage = new HashSet<UsageRecord>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id", nullable = true)
    private Host host;

    @Column(name = "algorithm", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerAlgorithm algorithm;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "account_id", nullable = false, length = 32)
    private Integer accountId;

    @Column(name = "connection_logging", nullable = false)
    private Boolean connectionLogging;

    @JoinColumn(name = "protocol", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerProtocol protocol;

    @JoinColumn(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerStatus status;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private ConnectionThrottle connectionThrottle;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private HealthMonitor healthMonitor;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar updated;

    @Transient
    private VirtualIpDozerWrapper virtualIpDozerWrapper;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        if (loadBalancerJoinVipSet == null) loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<UsageRecord> getUsage() {
        return usage;
    }

    public void setUsage(Set<UsageRecord> usage) {
        this.usage = usage;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public HealthMonitor getHealthMonitor() {
        return healthMonitor;
    }

    public void setHealthMonitor(HealthMonitor healthMonitor) {
        this.healthMonitor = healthMonitor;
    }

    public Boolean isConnectionLogging() {
        return connectionLogging;
    }

    public void setConnectionLogging(Boolean connectionLogging) {
        this.connectionLogging = connectionLogging;
    }

    public ConnectionThrottle getConnectionThrottle() {
        return connectionThrottle;
    }

    public void setConnectionThrottle(ConnectionThrottle connectionThrottle) {
        this.connectionThrottle = connectionThrottle;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getUpdated() {
        return updated;
    }

    public void setUpdated(Calendar updated) {
        this.updated = updated;
    }

    public LoadBalancerAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(LoadBalancerAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public LoadBalancerProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(LoadBalancerProtocol protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public LoadBalancerStatus getStatus() {
        return status;
    }

    public void setStatus(LoadBalancerStatus status) {
        this.status = status;
    }

    private static String valueOrNull(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public void addNode(Node node) {
        node.setLoadbalancer(this);
        nodes.add(node);
    }

    public boolean isUsingSsl() {
        return (protocol.equals(LoadBalancerProtocol.HTTPS) ||
                protocol.equals(LoadBalancerProtocol.IMAPS) ||
                protocol.equals(LoadBalancerProtocol.LDAPS) ||
                protocol.equals(LoadBalancerProtocol.POP3S));
    }

    public void getIpv6Servicenet(String throwaway) {
    }

    public void setIpv6Public(String throwaway) {

    }

    public void setIpv4Servicenet(String throwaway) {
    }

    public void setIpv4Public(String throwaway) {
    }

    public Set<LoadBalancerJoinVip6> getLoadBalancerJoinVip6Set() {
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
