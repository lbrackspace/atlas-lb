package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hostsubnet
        implements Serializable {

    private final static long serialVersionUID = 532512316L;
    protected List<NetInterface> netInterfaces;
    protected String name;

    public List<NetInterface> getNetInterfaces() {
        if (netInterfaces == null) {
            netInterfaces = new ArrayList<NetInterface>();
        }
        return this.netInterfaces;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setNetInterfaces(List<NetInterface> netInterfaces) {
        this.netInterfaces = netInterfaces;
    }
}
