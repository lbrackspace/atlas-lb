package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "lb_ssl")
public class SslTermination extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @OneToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "pem_key", nullable = true, columnDefinition = "mediumtext")
    private String privatekey;

    @Column(name = "pem_cert", nullable = true, columnDefinition = "mediumtext")
    private String certificate;

    @Column(name = "intermediate_certificate", nullable = true, columnDefinition = "mediumtext")
    private String intermediateCertificate;

    @Column(name = "enabled", nullable = true)
    private boolean enabled = true;

    @Column(name = "secure_port", nullable = true)
    private int securePort;

    @Column(name = "secure_traffic_only", nullable = true)
    private boolean secureTrafficOnly;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIntermediateCertificate() {
        return intermediateCertificate;
    }

    public void setIntermediateCertificate(String intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }

    public boolean isSecureTrafficOnly() {
        return secureTrafficOnly;
    }

    public void setSecureTrafficOnly(boolean secureTrafficOnly) {
        this.secureTrafficOnly = secureTrafficOnly;
    }

    public int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }
}
