package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.Type;

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

    @Column(name = "tls10_enabled", nullable = true)
    private boolean isTls10Enabled = true;

    @ManyToOne
    @JoinColumn(name = "cipher_profile",nullable = true,referencedColumnName = "name")
    private SslCipherProfile cipherProfile;

    @Column(name = "cipher_list", nullable = true, length = 65535)
    @Type(type="text")
    private String cipherList;
    
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

    public boolean isTls10Enabled() {
        return isTls10Enabled;
    }

    public void setTls10Enabled(boolean isTls10Enabled) {
        this.isTls10Enabled = isTls10Enabled;
    }

    public SslCipherProfile getCipherProfile() {
        return cipherProfile;
    }

    public void setCipherProfile(SslCipherProfile cipherProfile) {
        this.cipherProfile = cipherProfile;
    }

    public String getCipherList() {
        return cipherList;
    }

    public void setCipherList(String cipherList) {
        this.cipherList = cipherList;
    }
}
