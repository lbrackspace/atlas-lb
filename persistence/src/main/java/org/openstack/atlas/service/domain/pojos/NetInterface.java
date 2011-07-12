package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NetInterface
        implements Serializable {

    private final static long serialVersionUID = 532512316L;
    protected List<Cidr> cidrs;
    protected String name;

    public List<Cidr> getCidrs() {
        if (cidrs == null) {
            cidrs = new ArrayList<Cidr>();
        }
        return this.cidrs;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setCidrs(List<Cidr> cidrs) {
        this.cidrs = cidrs;
    }
}
