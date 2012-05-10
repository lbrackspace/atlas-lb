package org.openstack.atlas.atom.pojo;

import com.rackspace.docs.usage.core.V1Element;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/core", name = "usage")
@XmlSeeAlso({LBaaSUsagePojo.class, AccountLBaaSUsagePojo.class})
public class UsageV1Pojo extends V1Element {

}
