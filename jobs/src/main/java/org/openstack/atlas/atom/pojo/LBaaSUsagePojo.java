package org.openstack.atlas.atom.pojo;


import com.rackspace.docs.usage.lbaas.CloudLoadBalancersType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://docs.rackspace.com/usage/lbaas", name="CloudLoadBalancersType")
public class LBaaSUsagePojo extends CloudLoadBalancersType {

}