package org.openstack.atlas.persistence.rax.entities;

import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;
import org.openstack.atlas.util.ip.IPv6;
import org.openstack.atlas.util.ip.IPv6Cidr;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "virtual_ip_ipv6")
public class VirtualIpv6 extends org.openstack.atlas.service.domain.entities.VirtualIpv6 implements Serializable {

}
