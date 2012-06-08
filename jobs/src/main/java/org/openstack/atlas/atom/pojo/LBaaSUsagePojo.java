package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.CloudLoadBalancersUsageType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/core/usage/lbaas", name="loadBalancerUsageRecord")
public class LBaaSUsagePojo extends CloudLoadBalancersUsageType {

}