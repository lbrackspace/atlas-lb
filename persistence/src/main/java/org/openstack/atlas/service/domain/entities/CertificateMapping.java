package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "certificate_mapping")
public class CertificateMapping extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @ManyToOne
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "host_name", nullable = false, length = 128)
    private String hostName;

    @Column(name = "pem_key", nullable = false, columnDefinition = "mediumtext")
    private String privateKey;

    @Column(name = "pem_cert", nullable = false, columnDefinition = "mediumtext")
    private String certificate;

    @Column(name = "intermediate_certificate", nullable = true, columnDefinition = "mediumtext")
    private String intermediateCertificate;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getIntermediateCertificate() {
        return intermediateCertificate;
    }

    public void setIntermediateCertificate(String intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }

}
