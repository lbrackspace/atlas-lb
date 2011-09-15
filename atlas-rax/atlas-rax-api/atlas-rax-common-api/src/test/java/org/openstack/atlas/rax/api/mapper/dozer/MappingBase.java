package org.openstack.atlas.rax.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.BeforeClass;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;

import java.util.ArrayList;
import java.util.List;

public class MappingBase {
    protected static final String configDozerConfigFile = "dozer-config-map.xml";
    protected static final String healthMonitorDozerConfigFile = "dozer-health-monitor-map.xml";
    protected static final String loadBalancerDozerConfigFile = "dozer-rax-load-balancer-map.xml";
    protected static final String nodeDozerConfigFile = "dozer-node-map.xml";
    protected static final String virtualIpDozerConfigFile = "dozer-virtual-ip-map.xml";
    protected static DozerBeanMapper mapper;

    @BeforeClass
    public static void setupMapper() {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add(configDozerConfigFile);
        mappingFiles.add(healthMonitorDozerConfigFile);
        mappingFiles.add(loadBalancerDozerConfigFile);
        mappingFiles.add(nodeDozerConfigFile);
        mappingFiles.add(virtualIpDozerConfigFile);
        mapper = MapperBuilder.getConfiguredMapper(mappingFiles);
    }
}
