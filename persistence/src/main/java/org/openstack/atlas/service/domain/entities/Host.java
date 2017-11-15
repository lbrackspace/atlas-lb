package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name = "rest_endpoint", nullable = false)
    private String restEndpoint;

    @Column(name = "traffic_manager_name", nullable = false)
    private String trafficManagerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone", length = 4)
    private Zone zone;

    @Column(name = "soap_endpoint_active")
    private Boolean soapEndpointActive;

    @Column(name = "rest_endpoint_active")
    private Boolean restEndpointActive;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "host")
    @OrderBy("id")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Backup> backups = new HashSet<Backup>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
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

    public HostStatus getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(HostStatus hostStatus) {
        this.hostStatus = hostStatus;
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

    public Boolean getSoapEndpointActive() {
        return soapEndpointActive;
    }

    public void setSoapEndpointActive(Boolean soapEndpointActive) {
        this.soapEndpointActive = soapEndpointActive;
    }

    public String getRestEndpoint() {
        return restEndpoint;
    }

    public void setRestEndpoint(String restEndpoint) {
        this.restEndpoint = restEndpoint;
    }

    public Boolean getRestEndpointActive() {
        return restEndpointActive;
    }

    public void setRestEndpointActive(Boolean restEndpointActive) {
        this.restEndpointActive = restEndpointActive;
    }

    public Set<Backup> getBackups() {
        return backups;
    }

    public void setBackups(Set<Backup> backups) {
        this.backups = backups;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(String.format("id=%s, ", vorn(this.getId())));
        sb.append(String.format("name= \"%s\",", vorn(this.getName())));
        sb.append(String.format("clusterid = %s, ", this.getCluster() == null ? "" : this.getCluster().getId()));
        sb.append(String.format("hoststatus= \"%s\", ", vorn(this.getHostStatus())));
        sb.append(String.format("managementip = \"%s\", ", vorn(this.getManagementIp())));
        sb.append(String.format("maxconnections= %s, ", vorn(this.getMaxConcurrentConnections())));
        sb.append(String.format("trafficManagerName = \"%s\", ", vorn(this.getTrafficManagerName())));
        sb.append(String.format("coreid= %s,", vorn(this.getCoreDeviceId())));
        sb.append(String.format("endpoint=\"%s\",", vorn(this.getEndpoint())));
        sb.append(String.format("endpointActive=\"%s\"", vorn(this.getSoapEndpointActive())));
        sb.append("}");

        return sb.toString();
    }

    public String getTrafficManagerName() {
        return trafficManagerName;
    }

    public void setTrafficManagerName(String trafficManagerName) {
        this.trafficManagerName = trafficManagerName;
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

}
