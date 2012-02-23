package org.openstack.atlas.api.helpers;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.util.ip.DnsUtil;

import javax.naming.NamingException;
import java.util.List;

public class DomainVerificationHelper {
    public static boolean verifyDomain(Node node) {
        try {
            List<String> names = DnsUtil.lookup(node.getAddress(), "A");
            return !names.isEmpty();
        } catch (NamingException ne) {
           return false;
        }
    }
}
