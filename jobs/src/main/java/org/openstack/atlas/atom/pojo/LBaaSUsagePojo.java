package org.openstack.atlas.atom.pojo;


import org.openstack.atlas.jobs.LbaasUsage;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas", name="lbaas")
public class LBaaSUsagePojo extends LbaasUsage {

}