package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hostssubnet
        implements Serializable {

    private final static long serialVersionUID = 532512316L;
    protected List<Hostsubnet> hostssubnets;

    public List<Hostsubnet> getHostsubnets() {
        if (hostssubnets == null) {
            hostssubnets = new ArrayList<Hostsubnet>();
        }
        return this.hostssubnets;
    }

    public void setHostsubnets(List<Hostsubnet> hostssubnets) {
        this.hostssubnets = hostssubnets;
    }
}
