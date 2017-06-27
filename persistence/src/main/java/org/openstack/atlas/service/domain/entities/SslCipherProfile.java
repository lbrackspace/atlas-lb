package org.openstack.atlas.service.domain.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@javax.persistence.Entity
@Table(name = "ssl_cipher_profile")
public class SslCipherProfile extends Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @OneToMany(mappedBy = "sslCipherProfile" , cascade = CascadeType.ALL)
    List<SslTermination> sslTerminations = new ArrayList<SslTermination>();

    @Column(name = "name", nullable = false,unique = true)
    private String name;
    @Column(name = "ciphers", nullable = false, length = 1024)
    private String ciphers;
    @Column(name = "comments", nullable = true, length = 256)
    private String comments;

    public List<SslTermination> getSslTerminations() {
        return sslTerminations;
    }

    public void setSslTerminations(List<SslTermination> sslTerminations) {
        this.sslTerminations = sslTerminations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
