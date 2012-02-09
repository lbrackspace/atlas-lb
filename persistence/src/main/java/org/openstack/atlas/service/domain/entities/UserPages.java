package org.openstack.atlas.service.domain.entities;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "user_pages")
public class UserPages extends Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @OneToOne(fetch=FetchType.LAZY,optional=false)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    @JoinColumn(name = "loadbalancer_id")
    private LoadBalancer loadbalancer;

    @Column(name = "errorpage", nullable = true, length = 32, columnDefinition = "mediumtext")
    private String errorpage;

    public LoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(LoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getErrorpage() {
        return errorpage;
    }

    public void setErrorpage(String errorpage) {
        this.errorpage = errorpage;
    }
}
