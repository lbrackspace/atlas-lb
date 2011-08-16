package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.common.ip.exception.IPBlocksOverLapException;
import org.openstack.atlas.common.ip.exception.IPOctetOutOfRangeException;
import org.openstack.atlas.common.ip.exception.IPRangeTooBigException;
import org.openstack.atlas.common.ip.exception.IPStringConversionException;
import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojo.VirtualIpBlocks;

import java.util.List;

public interface ClusterService {

}
