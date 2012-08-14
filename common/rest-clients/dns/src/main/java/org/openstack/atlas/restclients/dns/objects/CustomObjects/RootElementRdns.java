package org.openstack.atlas.restclients.dns.objects.CustomObjects;

import javax.xml.bind.annotation.XmlRootElement;
import org.openstack.atlas.restclients.dns.objects.Rdns;

@XmlRootElement(name = "rdns", namespace = "http://docs.rackspacecloud.com/dns/api/v1.0")
public class RootElementRdns extends Rdns {
}
