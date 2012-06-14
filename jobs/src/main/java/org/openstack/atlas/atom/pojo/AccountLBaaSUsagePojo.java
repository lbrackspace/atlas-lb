package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.account.CloudLoadBalancersType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas/account", name="product")
public class AccountLBaaSUsagePojo extends CloudLoadBalancersType {

}