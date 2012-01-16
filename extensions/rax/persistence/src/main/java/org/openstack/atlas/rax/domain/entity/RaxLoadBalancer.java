package org.openstack.atlas.rax.domain.entity;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.openstack.atlas.service.domain.entity.LoadBalancer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@DiscriminatorValue(Discriminator.RAX)
public class RaxLoadBalancer extends LoadBalancer implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OrderBy("id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<RaxAccessList> accessLists = new HashSet<RaxAccessList>();

    @Column(name = "crazy_name", length = 128)
    private String crazyName = "WeeWooWoo!";

    @Column(name = "connection_logging", nullable = false)
    private Boolean connectionLogging = false;

    @OneToOne(mappedBy = "loadbalancer",fetch=FetchType.LAZY,optional=true)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private RaxUserPages userPages;

    public Set<RaxAccessList> getAccessLists() {
        return accessLists;
    }

    public void setAccessLists(Set<RaxAccessList> accessLists) {
        this.accessLists = accessLists;
    }

    public void addAccessList(RaxAccessList accessList) {
        accessList.setLoadbalancer(this);
        accessLists.add(accessList);
    }

    public String getCrazyName() {
        return crazyName;
    }

    public void setCrazyName(String crazyName) {
        this.crazyName = crazyName;
    }

    public Boolean getConnectionLogging() {
        return connectionLogging;
    }

    public void setConnectionLogging(Boolean connectionLogging) {
        this.connectionLogging = connectionLogging;
    }

    public RaxUserPages getUserPages() {
        return userPages;
    }

    public void setUserPages(RaxUserPages userPages) {
        this.userPages = userPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RaxLoadBalancer)) return false;

        RaxLoadBalancer that = (RaxLoadBalancer) o;

        if (accessLists != null ? !accessLists.equals(that.accessLists) : that.accessLists != null) return false;
        if (crazyName != null ? !crazyName.equals(that.crazyName) : that.crazyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accessLists != null ? accessLists.hashCode() : 0;
        result = 31 * result + (crazyName != null ? crazyName.hashCode() : 0);
        return result;
    }
}
