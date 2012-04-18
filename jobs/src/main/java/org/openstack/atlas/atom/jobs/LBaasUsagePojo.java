package org.openstack.atlas.atom.jobs;

import org.openstack.atlas.jobs.LBaaSUsage;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas", name = "LBaaSUsage")
public class LBaasUsagePojo extends LBaaSUsage {

}
