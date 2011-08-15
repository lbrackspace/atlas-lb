package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@javax.persistence.Entity
@Table(name = "host")
public class Host extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ipv6_servicenet", nullable = true)
    private String ipv6Servicenet;

    @Column(name = "ipv6_public", nullable = true)
    private String ipv6Public;

    @Column(name = "ipv4_servicenet", nullable = true)
    private String ipv4Servicenet;

    @Column(name = "ipv4_public", nullable = true)
    private String ipv4Public;

    @Column(name = "core_device_id", nullable = false)
    private String coreDeviceId;

    @Column(name = "max_concurrent_connections", nullable = false)
    private Integer maxConcurrentConnections;

    @Enumerated(EnumType.STRING)
    @Column(name = "host_status", length = 32, nullable = false)
    private HostStatus hostStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;

    @Column(name = "management_ip", nullable = false)
    private String managementIp;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "host_id")
    private List<LoadBalancer> loadbalancers;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "endpoint_active")
    private Boolean endpointActive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoreDeviceId() {
        return coreDeviceId;
    }

    public void setCoreDeviceId(String coreDeviceId) {
        this.coreDeviceId = coreDeviceId;
    }

    public Integer getMaxConcurrentConnections() {
        return maxConcurrentConnections;
    }

    public void setMaxConcurrentConnections(Integer maxConcurrentConnections) {
        this.maxConcurrentConnections = maxConcurrentConnections;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) {
        this.managementIp = managementIp;
    }

    private static String vorn(Object obj) {
        return obj == null ? "null" : obj.toString();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Boolean isEndpointActive() {
        return endpointActive;
    }

    public void setEndpointActive(Boolean endpointActive) {
        this.endpointActive = endpointActive;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(String.format("id=%s, ", vorn(this.getId())));
        sb.append(String.format("name= \"%s\",", vorn(this.getName())));
        sb.append(String.format("clusterid = %s, ", this.getCluster() == null ? "" : this.getCluster().getId()));
        sb.append(String.format("managementip = \"%s\", ", vorn(this.getManagementIp())));
        sb.append(String.format("maxconnections= %s, ", vorn(this.getMaxConcurrentConnections())));
        sb.append(String.format("coreid= %s,", vorn(this.getCoreDeviceId())));
        sb.append(String.format("endpoint=\"%s\",", vorn(this.getEndpoint())));
        sb.append(String.format("endpointActive=\"%s\"", vorn(this.isEndpointActive())));
        sb.append("}");

        return sb.toString();
    }

    public String getIpv6Servicenet() {
        return ipv6Servicenet;
    }

    public void setIpv6Servicenet(String ipv6Servicenet) {
        this.ipv6Servicenet = ipv6Servicenet;
    }

    public String getIpv6Public() {
        return ipv6Public;
    }

    public void setIpv6Public(String ipv6Public) {
        this.ipv6Public = ipv6Public;
    }

    public String getIpv4Public() {
        return ipv4Public;
    }

    public void setIpv4Public(String ipv4Public) {
        this.ipv4Public = ipv4Public;
    }

    public String getIpv4Servicenet() {
        return ipv4Servicenet;
    }

    public void setIpv4Servicenet(String ipv4Servicenet) {
        this.ipv4Servicenet = ipv4Servicenet;
    }

    public HostStatus getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(HostStatus hostStatus) {
        this.hostStatus = hostStatus;
    }
}
