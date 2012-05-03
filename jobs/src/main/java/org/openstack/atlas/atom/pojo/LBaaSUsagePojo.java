package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.LbaasUsage;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas", name="lbaas")
public class LBaaSUsagePojo extends LbaasUsage {

}