package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.service.domain.entity.LoadBalancer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@DiscriminatorValue("RAX")
public class RaxLoadBalancer extends LoadBalancer {
    private final static long serialVersionUID = 532512316L;

    @OrderBy("id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loadbalancer", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AccessList> accessLists = new HashSet<AccessList>();

    @Column(name = "crazy_name", length = 128)
    private String crazyName = "WeeWooWoo!";

    public Set<AccessList> getAccessLists() {
        return accessLists;
    }

    public void setAccessLists(Set<AccessList> accessLists) {
        this.accessLists = accessLists;
    }

    public void addAccessList(AccessList accessList) {
        accessList.setLoadbalancer(this);
        accessLists.add(accessList);
    }

    public String getCrazyName() {
        return crazyName;
    }

    public void setCrazyName(String crazyName) {
        this.crazyName = crazyName;
    }
}
