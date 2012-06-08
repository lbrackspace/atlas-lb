package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.account.CloudAccountLoadBalancersType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas/account")
public class AccountLBaaSUsagePojo extends CloudAccountLoadBalancersType {

}