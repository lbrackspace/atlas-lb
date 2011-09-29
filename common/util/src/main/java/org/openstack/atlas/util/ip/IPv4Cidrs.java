package org.openstack.atlas.util.ip;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public boolean contains(String ip) throws IPStringConversionException, IpTypeMissMatchException {
        for(IPv4Cidr cidr : cidrs){
            if(cidr.contains(ip)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getCidrsContainingIp(String ip) {
        List<String> cidrStrings = new ArrayList<String>();
        for(IPv4Cidr cidr : cidrs){
            try {
                if (cidr.contains(ip)) {
                    cidrStrings.add(cidr.getCidr());
                }
            } catch (IPStringConversionException ex) {
                continue;
            } catch (IpTypeMissMatchException ex) {
                continue;
            }
        }
        return cidrStrings;
    }
}
