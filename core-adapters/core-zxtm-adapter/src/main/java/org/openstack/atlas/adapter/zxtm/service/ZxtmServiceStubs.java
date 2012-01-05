package org.openstack.atlas.adapter.zxtm.service;

import com.zxtm.service.client.*;
import org.apache.axis.AxisFault;

import java.net.URL;

public class ZxtmServiceStubs {
    private PoolBindingStub zxtmPoolService;
    private SystemBackupsBindingStub zxtmSystemBackupsService;
    private SystemMachineInfoBindingStub zxtmSystemMachineInfoService;
    private TrafficIPGroupsBindingStub zxtmTrafficIpGroupService;
    private VirtualServerBindingStub zxtmVirtualServerService;
    private CatalogMonitorBindingStub zxtmMonitorCatalogService;
    private CatalogRateBindingStub zxtmRateCatalogService;
    private CatalogPersistenceBindingStub zxtmPersistenceService;
    private CatalogProtectionBindingStub zxtmProtectionService;
    private CatalogRuleBindingStub zxtmRuleCatalogService;
    private SystemStatsBindingStub zxtmSystemStatsService;
    private ConfExtraBindingStub zxtmConfExtraService;

    public ZxtmServiceStubs(PoolBindingStub zxtmPoolService,
                            SystemBackupsBindingStub zxtmSystemBackupsService, SystemMachineInfoBindingStub zxtmSystemMachineInfoService,
                            TrafficIPGroupsBindingStub zxtmTrafficIpGroupService, VirtualServerBindingStub zxtmVirtualServerService,
                            CatalogMonitorBindingStub zxtmMonitorCatalogService, CatalogPersistenceBindingStub zxtmPersistenceService,
                            CatalogProtectionBindingStub zxtmProtectionService, CatalogRuleBindingStub zxtmRuleCatalogService,
                            SystemStatsBindingStub zxtmSystemStatsService, CatalogRateBindingStub zxtmRateCatalogService,
                            ConfExtraBindingStub zxtmConfExtraService) {
        this.zxtmPoolService = zxtmPoolService;
        this.zxtmSystemBackupsService = zxtmSystemBackupsService;
        this.zxtmSystemMachineInfoService = zxtmSystemMachineInfoService;
        this.zxtmTrafficIpGroupService = zxtmTrafficIpGroupService;
        this.zxtmVirtualServerService = zxtmVirtualServerService;
        this.zxtmMonitorCatalogService = zxtmMonitorCatalogService;
        this.zxtmPersistenceService = zxtmPersistenceService;
        this.zxtmProtectionService = zxtmProtectionService;
        this.zxtmSystemStatsService = zxtmSystemStatsService;
        this.zxtmRuleCatalogService = zxtmRuleCatalogService;
        this.zxtmRateCatalogService = zxtmRateCatalogService;
        this.zxtmConfExtraService = zxtmConfExtraService;

    }

    public static ZxtmServiceStubs getServiceStubs(URL endpoint, String username, String password) throws AxisFault {
        PoolBindingStub zxtmPoolService = new PoolBindingStub(endpoint, null);
        zxtmPoolService.setUsername(username);
        zxtmPoolService.setPassword(password);

        SystemBackupsBindingStub zxtmSystemBackupsService = new SystemBackupsBindingStub(endpoint, null);
        zxtmSystemBackupsService.setUsername(username);
        zxtmSystemBackupsService.setPassword(password);

        SystemMachineInfoBindingStub zxtmSystemMachineInfoService = new SystemMachineInfoBindingStub(endpoint, null);
        zxtmSystemMachineInfoService.setUsername(username);
        zxtmSystemMachineInfoService.setPassword(password);

        TrafficIPGroupsBindingStub zxtmTrafficIpGroupService = new TrafficIPGroupsBindingStub(endpoint, null);
        zxtmTrafficIpGroupService.setUsername(username);
        zxtmTrafficIpGroupService.setPassword(password);

        VirtualServerBindingStub zxtmVirtualServerService = new VirtualServerBindingStub(endpoint, null);
        zxtmVirtualServerService.setUsername(username);
        zxtmVirtualServerService.setPassword(password);

        CatalogMonitorBindingStub zxtmMonitorCatalogService = new CatalogMonitorBindingStub(endpoint, null);
        zxtmMonitorCatalogService.setUsername(username);
        zxtmMonitorCatalogService.setPassword(password);

        CatalogPersistenceBindingStub zxtmMonitorPersistenceService = new CatalogPersistenceBindingStub(endpoint, null);
        zxtmMonitorPersistenceService.setUsername(username);
        zxtmMonitorPersistenceService.setPassword(password);

        CatalogProtectionBindingStub zxtmMonitorProtectionService = new CatalogProtectionBindingStub(endpoint, null);
        zxtmMonitorProtectionService.setUsername(username);
        zxtmMonitorProtectionService.setPassword(password);

        CatalogRateBindingStub zxtmRateCatalogService = new CatalogRateBindingStub(endpoint, null);
        zxtmRateCatalogService.setUsername(username);
        zxtmRateCatalogService.setPassword(password);

        CatalogRuleBindingStub zxtmRuleCatalogService = new CatalogRuleBindingStub(endpoint, null);
        zxtmRuleCatalogService.setUsername(username);
        zxtmRuleCatalogService.setPassword(password);

        SystemStatsBindingStub zxtmSystemStatsService = new SystemStatsBindingStub(endpoint, null);
        zxtmSystemStatsService.setUsername(username);
        zxtmSystemStatsService.setPassword(password);

        ConfExtraBindingStub zxtmConfExtraService = new ConfExtraBindingStub(endpoint,null);
        zxtmConfExtraService.setUsername(username);
        zxtmConfExtraService.setPassword(password);

        return new ZxtmServiceStubs(zxtmPoolService,
                zxtmSystemBackupsService, zxtmSystemMachineInfoService,
                zxtmTrafficIpGroupService, zxtmVirtualServerService,
                zxtmMonitorCatalogService, zxtmMonitorPersistenceService,
                zxtmMonitorProtectionService, zxtmRuleCatalogService,
                zxtmSystemStatsService, zxtmRateCatalogService,
                zxtmConfExtraService);
    }

    public PoolBindingStub getPoolBinding() {
        return zxtmPoolService;
    }

    public SystemBackupsBindingStub getSystemBackupsBinding() {
        return zxtmSystemBackupsService;
    }

    public SystemMachineInfoBindingStub getSystemMachineInfoBinding() {
        return zxtmSystemMachineInfoService;
    }

    public TrafficIPGroupsBindingStub getTrafficIpGroupBinding() {
        return zxtmTrafficIpGroupService;
    }

    public VirtualServerBindingStub getVirtualServerBinding() {
        return zxtmVirtualServerService;
    }

    public CatalogMonitorBindingStub getMonitorBinding() {
        return zxtmMonitorCatalogService;
    }

    public CatalogPersistenceBindingStub getPersistenceBinding() {
        return zxtmPersistenceService;
    }

    public CatalogProtectionBindingStub getProtectionBinding() {
        return zxtmProtectionService;
    }

    public SystemStatsBindingStub getSystemStatsBinding() {
        return zxtmSystemStatsService;
    }

    public CatalogRuleBindingStub getZxtmRuleCatalogService() {
        return zxtmRuleCatalogService;
    }

    public CatalogRateBindingStub getZxtmRateCatalogService() {
        return zxtmRateCatalogService;
    }

    public ConfExtraBindingStub getZxtmConfExtraService() {
        return zxtmConfExtraService;
    }
}
