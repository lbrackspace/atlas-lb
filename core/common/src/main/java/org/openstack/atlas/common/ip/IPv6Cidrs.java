package org.openstack.atlas.common.ip;

import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;

import java.util.ArrayList;
import java.util.List;

public class IPv6Cidrs {
    private List<IPv6Cidr> cidrs;

    public IPv6Cidrs(){
    }

    public List<IPv6Cidr> getCidrs() {
        if(cidrs == null){
            cidrs = new ArrayList<IPv6Cidr>();
        }
        return cidrs;
    }

    public void setCidrs(List<IPv6Cidr> cidrs) {
        this.cidrs = cidrs;
    }

    public boolean contains(String ip) throws IPStringConversionException1, IpTypeMissMatchException {
        for(IPv6Cidr cidr : cidrs){
            if(cidr.contains(ip)) {
                return true;
            }

        }
        return false;
    }
}
