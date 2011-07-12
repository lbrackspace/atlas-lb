package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
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

    public boolean contains(String ip) throws IPStringConversionException, IpTypeMissMatchException {
        for(IPv6Cidr cidr : cidrs){
            if(cidr.contains(ip)) {
                return true;
            }

        }
        return false;
    }
}
