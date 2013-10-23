package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import org.openstack.atlas.service.domain.pojos.VirtualIpDozerWrapper;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Table(name = "loadbalancer")
public class LoadBalancer extends Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;
    @Column(name = "name", length = 128)
    private String name;

    @OneToMany(mappedBy = "loadBalancer", fetch = FetchType.EAGER)
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

    @OneToMany(mappedBy = "loadBalancer", fetch = FetchType.EAGER)
    private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.EAGER)
    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Node> nodes = new HashSet<Node>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.LAZY)
    @OrderBy("id")
    private Set<Usage> usage = new HashSet<Usage>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.EAGER)
    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<AccessList> accessLists = new HashSet<AccessList>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.EAGER)
    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<LoadbalancerMeta> loadbalancerMetadata = new HashSet<LoadbalancerMeta>();

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

    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @Column(name = "connection_logging", nullable = false)
    private Boolean connectionLogging;

    @Column(name = "content_caching", nullable = false)
    private Boolean contentCaching;

    @Column(name = "https_redirect", nullable = false)
    private Boolean httpsRedirect;

    @JoinColumn(name = "protocol", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerProtocol protocol;

    @Column(name = "half_closed", nullable = false)
    private Boolean halfClosed;

    @JoinColumn(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoadBalancerStatus status;

    @JoinColumn(name = "sessionPersistence", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionPersistence sessionPersistence;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private ConnectionLimit connectionLimit;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private HealthMonitor healthMonitor;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private Suspension suspension;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private RateLimit rateLimit;

    @OneToOne(mappedBy = "loadbalancer", fetch = FetchType.LAZY, optional = true)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private UserPages userPages;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE}, mappedBy = "loadbalancer")
    private SslTermination sslTermination;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar updated;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar provisioned;

    @Column(name = "is_sticky", nullable = false)
    private boolean isSticky;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", fetch = FetchType.EAGER)
    @OrderBy("ticketId")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Ticket> tickets = new HashSet<Ticket>();

    @Transient
    private VirtualIpDozerWrapper virtualIpDozerWrapper;

    @Transient
    private SourceAddresses sourceAddresses;

    public SourceAddresses getSourceAddresses() {
        return sourceAddresses;
    }

    public void setSourceAddresses(SourceAddresses sourceAddresses) {
        this.sourceAddresses = sourceAddresses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Suspension getSuspension() {
        return suspension;
    }

    public void setSuspension(Suspension suspension) {
        this.suspension = suspension;
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

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public Set<Usage> getUsage() {
        return usage;
    }

    public void setUsage(Set<Usage> usage) {
        this.usage = usage;
    }

    public Set<AccessList> getAccessLists() {
        return accessLists;
    }

    public void setAccessLists(Set<AccessList> accessLists) {
        this.accessLists = accessLists;
    }

    public Set<LoadbalancerMeta> getLoadbalancerMetadata() {
        return loadbalancerMetadata;
    }

    public void setLoadbalancerMetadata(Set<LoadbalancerMeta> metadata) {
        this.loadbalancerMetadata = metadata;
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

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Boolean isConnectionLogging() {
        return connectionLogging;
    }

    public void setConnectionLogging(Boolean connectionLogging) {
        this.connectionLogging = connectionLogging;
    }

    public Boolean isContentCaching() {
        return contentCaching;
    }

    public void setContentCaching(Boolean contentCaching) {
        this.contentCaching = contentCaching;
    }

    public Boolean isHttpsRedirect() {
        return httpsRedirect;
    }

    public void setHttpsRedirect(Boolean httpsRedirect) {
        this.httpsRedirect = httpsRedirect;
    }

    public ConnectionLimit getConnectionLimit() {
        return connectionLimit;
    }

    public void setConnectionLimit(ConnectionLimit connectionLimit) {
        this.connectionLimit = connectionLimit;
    }

    public SessionPersistence getSessionPersistence() {
        return this.sessionPersistence;
    }

    public void setSessionPersistence(SessionPersistence sessionPersistence) {
        this.sessionPersistence = sessionPersistence;
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

    public Calendar getProvisioned() {
        return provisioned;
    }

    public void setProvisioned(Calendar provisioned) {
        this.provisioned = provisioned;
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

    public Boolean isHalfClosed() {
        return halfClosed;
    }

    public void setHalfClosed(Boolean halfClosed) {
        this.halfClosed = halfClosed;
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

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    private static String valueOrNull(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public void addAccessList(AccessList accessList) {
        accessList.setLoadbalancer(this);
        accessLists.add(accessList);
    }

    public void addNode(Node node) {
        node.setLoadbalancer(this);
        nodes.add(node);
    }

    public void addMeta(LoadbalancerMeta loadbalancerMeta) {
        loadbalancerMeta.setLoadbalancer(this);
        loadbalancerMetadata.add(loadbalancerMeta);
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        isSticky = sticky;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public boolean isUsingSsl() {
        return (sslTermination != null && sslTermination.isEnabled());
    }

    public boolean hasSsl() {
        return (sslTermination != null);
    }

    public boolean isSecureOnly() {
        return (sslTermination != null && sslTermination.isSecureTrafficOnly());
    }

    public String getIpv6Servicenet() {
        return null;
//        if (host == null) {
//            return null;
//        }
//        return host.getIpv6Servicenet();
    }

    public String getIpv6Public() {
        return null;
//        if (host == null) {
//            return null;
//        }
//
//        return host.getIpv6Public();
    }

    public String getIpv4Servicenet() {
        return null;
//        if (host == null) {
//            return null;
//        }
//        return host.getIpv4Servicenet();
    }

    public String getIpv4Public() {
        return null;
//        if (host == null) {
//            return null;
//        }
//        return host.getIpv4Public();
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

    /**
     * @return the userPages
     */
    public UserPages getUserPages() {
        return userPages;
    }

    /**
     * @param userPages the userPages to set
     */
    public void setUserPages(UserPages userPages) {
        this.userPages = userPages;
    }

    public SslTermination getSslTermination() {
        return sslTermination;
    }

    public void setSslTermination(SslTermination sslTermination) {
        this.sslTermination = sslTermination;
    }
}
