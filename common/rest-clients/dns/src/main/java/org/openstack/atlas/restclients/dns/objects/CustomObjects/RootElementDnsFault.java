
package org.openstack.atlas.restclients.dns.objects.CustomObjects;

import javax.xml.bind.annotation.XmlRootElement;
import org.openstack.atlas.restclients.dns.objects.DnsFault;

@XmlRootElement(name="dnsFault",namespace = "http://docs.rackspacecloud.com/dns/api/v1.0")
public class RootElementDnsFault extends DnsFault {
}
