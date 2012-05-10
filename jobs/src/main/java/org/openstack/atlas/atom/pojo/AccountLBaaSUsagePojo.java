package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.AccountLoadBalancerUsage;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas", name="accountLoadBalancerUsageRecord")
public class AccountLBaaSUsagePojo extends AccountLoadBalancerUsage {

}