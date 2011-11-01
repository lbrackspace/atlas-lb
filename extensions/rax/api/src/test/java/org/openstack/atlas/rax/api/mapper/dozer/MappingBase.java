package org.openstack.atlas.rax.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.BeforeClass;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.datamodel.*;
import org.openstack.atlas.rax.datamodel.RaxAlgorithmType;
import org.openstack.atlas.rax.datamodel.RaxProtocolType;

import java.util.ArrayList;
import java.util.List;

public class MappingBase {
    protected static final String configDozerConfigFile = "dozer-config-map.xml";
    protected static final String healthMonitorDozerConfigFile = "dozer-health-monitor-map.xml";
    protected static final String loadBalancerDozerConfigFile = "dozer-rax-load-balancer-map.xml";
    protected static final String nodeDozerConfigFile = "dozer-node-map.xml";
    protected static final String sessionPersistenceDozerConfigFile = "dozer-session-persistence-map.xml";
    protected static final String virtualIpDozerConfigFile = "dozer-virtual-ip-map.xml";
    protected static DozerBeanMapper mapper;

    @BeforeClass
    public static void setupMapper() {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add(configDozerConfigFile);
        mappingFiles.add(healthMonitorDozerConfigFile);
        mappingFiles.add(loadBalancerDozerConfigFile);
        mappingFiles.add(nodeDozerConfigFile);
        mappingFiles.add(sessionPersistenceDozerConfigFile);
        mappingFiles.add(virtualIpDozerConfigFile);
        mapper = MapperBuilder.getConfiguredMapper(mappingFiles);
    }

    @BeforeClass
    public static void setupAutoWiredDependencies() {
        /* TODO: Figure out how to get rid of this hack */
        AtlasTypeHelper atlasTypeHelper = new AtlasTypeHelper();
        atlasTypeHelper.setAlgorithmType(new RaxAlgorithmType());
        atlasTypeHelper.setProtocolType(new RaxProtocolType());
        atlasTypeHelper.setLoadBalancerStatus(new CoreLoadBalancerStatus());
        atlasTypeHelper.setNodeStatus(new CoreNodeStatus());
        atlasTypeHelper.setPersistenceType(new CorePersistenceType());
        atlasTypeHelper.setHealthMonitorType(new CoreHealthMonitorType());
    }
}
