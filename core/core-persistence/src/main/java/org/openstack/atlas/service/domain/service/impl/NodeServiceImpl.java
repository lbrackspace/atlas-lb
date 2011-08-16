package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.converters.StringConverter;
import org.openstack.atlas.common.ip.exception.IPStringConversionException;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;
import org.openstack.atlas.service.domain.common.NodesHelper;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.pojo.NodeMap;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NodeServiceImpl implements NodeService {
    private final Log LOG = LogFactory.getLog(NodeServiceImpl.class);
}
