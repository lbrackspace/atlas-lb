package org.openstack.atlas.util.common;

import org.junit.Assert;
import org.junit.Test;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.HashMap;
import java.util.Map;

public class MapUtilTest {

    @Test
    public void shouldTransformUsagesGroupedByHostsToGroupedByLoadBalancers() {
        SnmpUsage host1lb1 = new SnmpUsage();
        host1lb1.setLoadbalancerId(1);
        host1lb1.setHostId(1);
        SnmpUsage host1lb2 = new SnmpUsage();
        host1lb2.setLoadbalancerId(2);
        host1lb2.setHostId(1);
        SnmpUsage host1lb3 = new SnmpUsage();
        host1lb3.setLoadbalancerId(3);
        host1lb3.setHostId(1);
        SnmpUsage host2lb1 = new SnmpUsage();
        host2lb1.setLoadbalancerId(1);
        host2lb1.setHostId(2);
        SnmpUsage host2lb2 = new SnmpUsage();
        host2lb2.setLoadbalancerId(2);
        host2lb2.setHostId(2);
        SnmpUsage host2lb3 = new SnmpUsage();
        host2lb3.setLoadbalancerId(3);
        host2lb3.setHostId(2);
        SnmpUsage host3lb1 = new SnmpUsage();
        host3lb1.setLoadbalancerId(1);
        host3lb1.setHostId(3);
        SnmpUsage host3lb2 = new SnmpUsage();
        host3lb2.setLoadbalancerId(2);
        host3lb2.setHostId(3);
        SnmpUsage host3lb3 = new SnmpUsage();
        host3lb3.setLoadbalancerId(3);
        host3lb3.setHostId(3);
        Map<Integer, Map<Integer, SnmpUsage>> groupedByHosts = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        Map<Integer, SnmpUsage> host1Map = new HashMap<Integer, SnmpUsage>();
        host1Map.put(1, host1lb1);
        host1Map.put(2, host1lb2);
        host1Map.put(3, host1lb3);
        groupedByHosts.put(1, host1Map);
        Map<Integer, SnmpUsage> host2Map = new HashMap<Integer, SnmpUsage>();
        host2Map.put(1, host2lb1);
        host2Map.put(2, host2lb2);
        host2Map.put(3, host2lb3);
        groupedByHosts.put(2, host2Map);
        Map<Integer, SnmpUsage> host3Map = new HashMap<Integer, SnmpUsage>();
        host3Map.put(1, host3lb1);
        host3Map.put(2, host3lb2);
        host3Map.put(3, host3lb3);
        groupedByHosts.put(3, host3Map);
        Map<Integer, Map<Integer, SnmpUsage>> lbMap = MapUtil.swapKeys(groupedByHosts);
        Assert.assertEquals(host1lb1.getHostId(), lbMap.get(1).get(1).getHostId());
        Assert.assertEquals(host1lb1.getLoadbalancerId(), lbMap.get(1).get(1).getLoadbalancerId());
        Assert.assertEquals(host2lb1.getHostId(), lbMap.get(1).get(2).getHostId());
        Assert.assertEquals(host2lb1.getLoadbalancerId(), lbMap.get(1).get(2).getLoadbalancerId());
        Assert.assertEquals(host3lb1.getHostId(), lbMap.get(1).get(3).getHostId());
        Assert.assertEquals(host3lb1.getLoadbalancerId(), lbMap.get(1).get(3).getLoadbalancerId());

        Assert.assertEquals(host1lb2.getHostId(), lbMap.get(2).get(1).getHostId());
        Assert.assertEquals(host1lb2.getLoadbalancerId(), lbMap.get(2).get(1).getLoadbalancerId());
        Assert.assertEquals(host2lb2.getHostId(), lbMap.get(2).get(2).getHostId());
        Assert.assertEquals(host2lb2.getLoadbalancerId(), lbMap.get(2).get(2).getLoadbalancerId());
        Assert.assertEquals(host3lb2.getHostId(), lbMap.get(2).get(3).getHostId());
        Assert.assertEquals(host3lb2.getLoadbalancerId(), lbMap.get(2).get(3).getLoadbalancerId());

        Assert.assertEquals(host1lb3.getHostId(), lbMap.get(3).get(1).getHostId());
        Assert.assertEquals(host1lb3.getLoadbalancerId(), lbMap.get(3).get(1).getLoadbalancerId());
        Assert.assertEquals(host2lb3.getHostId(), lbMap.get(3).get(2).getHostId());
        Assert.assertEquals(host2lb3.getLoadbalancerId(), lbMap.get(3).get(2).getLoadbalancerId());
        Assert.assertEquals(host3lb3.getHostId(), lbMap.get(3).get(3).getHostId());
        Assert.assertEquals(host3lb3.getLoadbalancerId(), lbMap.get(3).get(3).getLoadbalancerId());
    }

}
