package org.openstack.atlas.usage.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoadBalancerNameMap {
    private Integer loadBalancerId;
    private Integer accountId;
    private String nonSslVirtualServerName;
    private String sslVirtualServerName;

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getNonSslVirtualServerName() {
        return nonSslVirtualServerName;
    }

    public void setNonSslVirtualServerName(String nonSslVirtualServerName) {
        this.nonSslVirtualServerName = nonSslVirtualServerName;
    }

    public String getSslVirtualServerName() {
        return sslVirtualServerName;
    }

    public void setSslVirtualServerName(String sslVirtualServerName) {
        this.sslVirtualServerName = sslVirtualServerName;
    }

    public static List<String> loadBalancerNames(Collection<LoadBalancerNameMap> maps) {
        List<String> loadBalancerNames = new ArrayList<String>();

        for (LoadBalancerNameMap map : maps) {
            if(map.getNonSslVirtualServerName() != null && !map.getNonSslVirtualServerName().isEmpty()) {
                loadBalancerNames.add(map.getNonSslVirtualServerName());
            }
        }

        return loadBalancerNames;
    }

    public static List<String> loadBalancerSslNames(Collection<LoadBalancerNameMap> maps) {
        List<String> loadBalancerSslNames = new ArrayList<String>();

        for (LoadBalancerNameMap map : maps) {
            if(map.getSslVirtualServerName() != null && !map.getSslVirtualServerName().isEmpty()) {
                loadBalancerSslNames.add(map.getSslVirtualServerName());
            }
        }
        
        return loadBalancerSslNames;
    }
}
