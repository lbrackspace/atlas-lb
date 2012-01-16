package org.openstack.atlas.rax.domain.entity;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="vendor",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue(Discriminator.RAX)
@Table(name = "user_pages")
public class RaxUserPages extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {

    private final static long serialVersionUID = 532512316L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @OneToOne(fetch=FetchType.LAZY,optional=false)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    @JoinColumn(name = "loadbalancer_id")
    private RaxLoadBalancer loadbalancer;

    @Column(name = "errorpage", nullable = true, length = 32, columnDefinition = "mediumtext")
    private String errorpage;

    public RaxLoadBalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(RaxLoadBalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getErrorpage() {
        return errorpage;
    }

    public void setErrorpage(String errorpage) {
        this.errorpage = errorpage;
    }
}
