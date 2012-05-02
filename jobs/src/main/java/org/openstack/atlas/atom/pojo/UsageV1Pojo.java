package org.openstack.atlas.atom.pojo;

import org.openstack.atlas.jobs.UsageV1;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/core", name = "usage")
@XmlSeeAlso({LBaaSUsagePojo.class})
public class UsageV1Pojo extends UsageV1 {

}
