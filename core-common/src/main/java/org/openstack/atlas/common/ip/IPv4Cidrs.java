package org.openstack.atlas.common.ip;

import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;

import java.util.ArrayList;
import java.util.List;

public class IPv4Cidrs {
    private List<IPv4Cidr> cidrs;

    public IPv4Cidrs(){
    }

    public List<IPv4Cidr> getCidrs() {
        if(cidrs == null){
            cidrs = new ArrayList<IPv4Cidr>();
        }
        return cidrs;
    }

    public void setCidrs(List<IPv4Cidr> cidrs) {
        this.cidrs = cidrs;
    }

    public boolean contains(String ip) throws IPStringConversionException1, IpTypeMissMatchException {
        for(IPv4Cidr cidr : cidrs){
            if(cidr.contains(ip)) {
                return true;
            }

        }
        return false;
    }
}
