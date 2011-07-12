package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancerAudits implements Serializable {
    private final static long serialVersionUID = 532512316L;
    protected List<LoadBalancerAudit> loadBalancerAudits;

    public List<LoadBalancerAudit> getLoadBalancerAudits() {
        if (loadBalancerAudits == null) return new ArrayList<LoadBalancerAudit>();
        return loadBalancerAudits;
    }

    public void setLoadBalancerAudits(List<LoadBalancerAudit> loadBalancerAudits) {
        this.loadBalancerAudits = loadBalancerAudits;
    }
}

