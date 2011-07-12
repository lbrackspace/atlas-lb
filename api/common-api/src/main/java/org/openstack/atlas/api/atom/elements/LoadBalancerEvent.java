package org.openstack.atlas.api.atom.elements;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElement;

public interface LoadBalancerEvent extends ExtensibleElement, Element {
    public String getLoadBalancerId();
    public Element getLoadBalancerIdElement();
    public void setLoadBalancerId(String loadBalancerId);
    public void setLoadBalancerIdElement(String loadBalancerIdElement);
}
