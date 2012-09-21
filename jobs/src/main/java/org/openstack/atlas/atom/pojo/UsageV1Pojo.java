package org.openstack.atlas.atom.pojo;

import com.rackspace.docs.core.event.V1Element;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://docs.rackspace.com/core/event", name = "event")
@XmlSeeAlso({LBaaSUsagePojo.class})
public class UsageV1Pojo extends V1Element {

}
