package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.account.CloudAccountLoadBalancersType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas/account", name="CloudLoadBalancersType")
public class AccountLBaaSUsagePojo extends CloudAccountLoadBalancersType {

}