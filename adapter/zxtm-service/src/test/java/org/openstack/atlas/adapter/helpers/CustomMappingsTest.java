package org.openstack.atlas.adapter.helpers;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client.bandwidth.BandwidthProperties;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.exception.ClientException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorBasic;
import org.rackspace.stingray.client.monitor.MonitorHttp;
import org.rackspace.stingray.client.monitor.MonitorProperties;
import org.rackspace.stingray.client.pool.*;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionAccessRestriction;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.virtualserver.*;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class CustomMappingsTest extends STMTestBase {


    public static class WhenMappingVirtualServerStats {
        CustomMappings cMappings;
        Stats stats;
        List<VirtualServerStats> virtualServerStatsList;
        List<VirtualServerStats> virtualServerStatsSslList;
        VirtualServerStats virtualServerStats;
        VirtualServerStats virtualServerStatsSsl;
        VirtualServerStatsProperties vsProps;
        VirtualServerStatsProperties vsPropsSsl;

        @Before
        public void standUp() throws IPStringConversionException {
            cMappings = new CustomMappings();

            virtualServerStatsList = new ArrayList<>();
            virtualServerStatsSslList = new ArrayList<>();

            virtualServerStats = new VirtualServerStats();
            virtualServerStatsSsl = new VirtualServerStats();
            vsProps = new VirtualServerStatsProperties();
            vsPropsSsl = new VirtualServerStatsProperties();


            vsProps.setConnectionFailures(5);
            vsProps.setConnectionErrors(6);
            vsProps.setConnectTimedOut(7);
            vsProps.setDataTimedOut(8);
            vsProps.setKeepaliveTimedOut(9);
            vsProps.setMaxConn(10);
            vsProps.setCurrentConn(4);

            vsPropsSsl.setConnectionFailures(11);
            vsPropsSsl.setConnectionErrors(12);
            vsPropsSsl.setConnectTimedOut(13);
            vsPropsSsl.setDataTimedOut(14);
            vsPropsSsl.setKeepaliveTimedOut(15);
            vsPropsSsl.setMaxConn(16);
            vsPropsSsl.setCurrentConn(17);

            virtualServerStats.setStatistics(vsProps);
            virtualServerStatsSsl.setStatistics(vsPropsSsl);

            virtualServerStatsList.add(virtualServerStats);
            virtualServerStatsSslList.add(virtualServerStatsSsl);

        }

        @Test
        public void shouldMapVirtualServerNoSSL() {
            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, new ArrayList<>());
            Assert.assertEquals(5, stats.getConnectFailure()[0]);
            Assert.assertEquals(6, stats.getConnectError()[0]);
            Assert.assertEquals(7, stats.getConnectTimeOut()[0]);
            Assert.assertEquals(8, stats.getDataTimedOut()[0]);
            Assert.assertEquals(9, stats.getKeepAliveTimedOut()[0]);
            Assert.assertEquals(10, stats.getMaxConn()[0]);
            Assert.assertEquals(4, stats.getCurrentConn()[0]);
        }

        @Test
        public void shouldMapMultiVirtualServersNoSSL() {
            VirtualServerStats vs2 = new VirtualServerStats();
            VirtualServerStatsProperties vsp2 = new VirtualServerStatsProperties();

            vsp2.setConnectionFailures(5);
            vsp2.setConnectionErrors(6);
            vsp2.setConnectTimedOut(7);
            vsp2.setDataTimedOut(8);
            vsp2.setKeepaliveTimedOut(9);
            vsp2.setMaxConn(11); // maxconn is top value from shadow or regular server
            vsp2.setCurrentConn(4);
            vs2.setStatistics(vsp2);
            virtualServerStatsList.add(vs2);

            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, new ArrayList<>());
            Assert.assertEquals(10, stats.getConnectFailure()[0]);
            Assert.assertEquals(12, stats.getConnectError()[0]);
            Assert.assertEquals(14, stats.getConnectTimeOut()[0]);
            Assert.assertEquals(16, stats.getDataTimedOut()[0]);
            Assert.assertEquals(18, stats.getKeepAliveTimedOut()[0]);
            Assert.assertEquals(11, stats.getMaxConn()[0]);
            Assert.assertEquals(8, stats.getCurrentConn()[0]);
        }

        @Test
        public void shouldMapVirtualServerWithSSL() {
            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, virtualServerStatsSslList);
            Assert.assertEquals(5, stats.getConnectFailure()[0]);
            Assert.assertEquals(6, stats.getConnectError()[0]);
            Assert.assertEquals(7, stats.getConnectTimeOut()[0]);
            Assert.assertEquals(8, stats.getDataTimedOut()[0]);
            Assert.assertEquals(9, stats.getKeepAliveTimedOut()[0]);
            Assert.assertEquals(10, stats.getMaxConn()[0]);
            Assert.assertEquals(4, stats.getCurrentConn()[0]);

            Assert.assertEquals(11, stats.getConnectFailureSsl()[0]);
            Assert.assertEquals(12, stats.getConnectErrorSsl()[0]);
            Assert.assertEquals(13, stats.getConnectTimeOutSsl()[0]);
            Assert.assertEquals(14, stats.getDataTimedOutSsl()[0]);
            Assert.assertEquals(15, stats.getKeepAliveTimedOutSsl()[0]);
            Assert.assertEquals(16, stats.getMaxConnSsl()[0]);
            Assert.assertEquals(17, stats.getCurrentConnSsl()[0]);
        }

        @Test
        public void shouldMapMultiVirtualServersWithSSL() {
            VirtualServerStats vs2 = new VirtualServerStats();
            VirtualServerStatsProperties vsp2 = new VirtualServerStatsProperties();

            VirtualServerStats vs2ssl = new VirtualServerStats();
            VirtualServerStatsProperties vsp2ssl = new VirtualServerStatsProperties();

            vsp2.setConnectionFailures(5);
            vsp2.setConnectionErrors(6);
            vsp2.setConnectTimedOut(7);
            vsp2.setDataTimedOut(8);
            vsp2.setKeepaliveTimedOut(9);
            vsp2.setMaxConn(11); // maxconn is top value from shadow or regular server
            vsp2.setCurrentConn(4);
            vs2.setStatistics(vsp2);
            virtualServerStatsList.add(vs2);

            vsp2ssl.setConnectionFailures(11);
            vsp2ssl.setConnectionErrors(12);
            vsp2ssl.setConnectTimedOut(13);
            vsp2ssl.setDataTimedOut(14);
            vsp2ssl.setKeepaliveTimedOut(15);
            vsp2ssl.setMaxConn(17); // maxconn is top value from shadow or regular server
            vsp2ssl.setCurrentConn(17);
            vs2ssl.setStatistics(vsp2ssl);
            virtualServerStatsSslList.add(vs2ssl);


            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, virtualServerStatsSslList);
            Assert.assertEquals(10, stats.getConnectFailure()[0]);
            Assert.assertEquals(12, stats.getConnectError()[0]);
            Assert.assertEquals(14, stats.getConnectTimeOut()[0]);
            Assert.assertEquals(16, stats.getDataTimedOut()[0]);
            Assert.assertEquals(18, stats.getKeepAliveTimedOut()[0]);
            Assert.assertEquals(11, stats.getMaxConn()[0]);
            Assert.assertEquals(8, stats.getCurrentConn()[0]);

            Assert.assertEquals(22, stats.getConnectFailureSsl()[0]);
            Assert.assertEquals(24, stats.getConnectErrorSsl()[0]);
            Assert.assertEquals(26, stats.getConnectTimeOutSsl()[0]);
            Assert.assertEquals(28, stats.getDataTimedOutSsl()[0]);
            Assert.assertEquals(30, stats.getKeepAliveTimedOutSsl()[0]);
            Assert.assertEquals(17, stats.getMaxConnSsl()[0]);
            Assert.assertEquals(34, stats.getCurrentConnSsl()[0]);
        }
    }
}