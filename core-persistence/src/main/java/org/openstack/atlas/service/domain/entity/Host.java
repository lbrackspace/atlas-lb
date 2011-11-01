package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "host")
public class Host extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "endpoint_active", nullable = false)
    private Boolean endpointActive;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "ipv4_public", nullable = true)
    private String ipv4Public;

    @Column(name = "ipv4_service_net", nullable = true)
    private String ipv4ServiceNet;

    @Column(name = "ipv6_public", nullable = true)
    private String ipv6Public;

    @Column(name = "ipv6_service_net", nullable = true)
    private String ipv6ServiceNet;

    @Enumerated(EnumType.STRING)
    @Column(name = "host_status", length = 32, nullable = false)
    private HostStatus hostStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "host_id")
    private List<LoadBalancer> loadbalancers;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public Boolean isEndpointActive() {
        return endpointActive;
    }

    public void setEndpointActive(Boolean endpointActive) {
        this.endpointActive = endpointActive;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
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

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(String.format("id=%s, ", vorn(this.getId())));
        sb.append(String.format("name= \"%s\",", vorn(this.getName())));
        sb.append(String.format("clusterid = %s, ", this.getCluster() == null ? "" : this.getCluster().getId()));
        sb.append(String.format("endpoint=\"%s\",", vorn(this.getEndpoint())));
        sb.append("}");

        return sb.toString();
    }

    public String getIpv6ServiceNet() {
        return ipv6ServiceNet;
    }

    public void setIpv6ServiceNet(String ipv6ServiceNet) {
        this.ipv6ServiceNet = ipv6ServiceNet;
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

    public String getIpv4ServiceNet() {
        return ipv4ServiceNet;
    }

    public void setIpv4ServiceNet(String ipv4ServiceNet) {
        this.ipv4ServiceNet = ipv4ServiceNet;
    }

    public HostStatus getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(HostStatus hostStatus) {
        this.hostStatus = hostStatus;
    }
}
