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


            vsProps.setConnectionFailures(5L);
            vsProps.setConnectionErrors(6L);
            vsProps.setConnectTimedOut(7L);
            vsProps.setDataTimedOut(8L);
            vsProps.setKeepaliveTimedOut(9L);
            vsProps.setMaxConn(10L);
            vsProps.setCurrentConn(4L);

            vsPropsSsl.setConnectionFailures(11L);
            vsPropsSsl.setConnectionErrors(12L);
            vsPropsSsl.setConnectTimedOut(13L);
            vsPropsSsl.setDataTimedOut(14L);
            vsPropsSsl.setKeepaliveTimedOut(15L);
            vsPropsSsl.setMaxConn(16L);
            vsPropsSsl.setCurrentConn(17L);

            virtualServerStats.setStatistics(vsProps);
            virtualServerStatsSsl.setStatistics(vsPropsSsl);

            virtualServerStatsList.add(virtualServerStats);
            virtualServerStatsSslList.add(virtualServerStatsSsl);

        }

        @Test
        public void shouldMapVirtualServerNoSSL() {
            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, new ArrayList<>());
            Assert.assertEquals(5, stats.getConnectFailure());
            Assert.assertEquals(6, stats.getConnectError());
            Assert.assertEquals(7, stats.getConnectTimeOut());
            Assert.assertEquals(8, stats.getDataTimedOut());
            Assert.assertEquals(9, stats.getKeepAliveTimedOut());
            Assert.assertEquals(10, stats.getMaxConn());
            Assert.assertEquals(4, stats.getCurrentConn());
        }

        @Test
        public void shouldMapMultiVirtualServersNoSSL() {
            VirtualServerStats vs2 = new VirtualServerStats();
            VirtualServerStatsProperties vsp2 = new VirtualServerStatsProperties();

            vsp2.setConnectionFailures(5L);
            vsp2.setConnectionErrors(6L);
            vsp2.setConnectTimedOut(7L);
            vsp2.setDataTimedOut(8L);
            vsp2.setKeepaliveTimedOut(9L);
            vsp2.setMaxConn(11L); // maxconn is top value from shadow or regular server
            vsp2.setCurrentConn(4L);
            vs2.setStatistics(vsp2);
            virtualServerStatsList.add(vs2);

            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, new ArrayList<>());
            Assert.assertEquals(10, stats.getConnectFailure());
            Assert.assertEquals(12, stats.getConnectError());
            Assert.assertEquals(14, stats.getConnectTimeOut());
            Assert.assertEquals(16, stats.getDataTimedOut());
            Assert.assertEquals(18, stats.getKeepAliveTimedOut());
            Assert.assertEquals(11, stats.getMaxConn());
            Assert.assertEquals(8, stats.getCurrentConn());
        }

        @Test
        public void shouldMapVirtualServerWithSSL() {
            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, virtualServerStatsSslList);
            Assert.assertEquals(5, stats.getConnectFailure());
            Assert.assertEquals(6, stats.getConnectError());
            Assert.assertEquals(7, stats.getConnectTimeOut());
            Assert.assertEquals(8, stats.getDataTimedOut());
            Assert.assertEquals(9, stats.getKeepAliveTimedOut());
            Assert.assertEquals(10, stats.getMaxConn());
            Assert.assertEquals(4, stats.getCurrentConn());

            Assert.assertEquals(11, stats.getConnectFailureSsl());
            Assert.assertEquals(12, stats.getConnectErrorSsl());
            Assert.assertEquals(13, stats.getConnectTimeOutSsl());
            Assert.assertEquals(14, stats.getDataTimedOutSsl());
            Assert.assertEquals(15, stats.getKeepAliveTimedOutSsl());
            Assert.assertEquals(16, stats.getMaxConnSsl());
            Assert.assertEquals(17, stats.getCurrentConnSsl());
        }

        @Test
        public void shouldMapMultiVirtualServersWithSSL() {
            VirtualServerStats vs2 = new VirtualServerStats();
            VirtualServerStatsProperties vsp2 = new VirtualServerStatsProperties();

            VirtualServerStats vs2ssl = new VirtualServerStats();
            VirtualServerStatsProperties vsp2ssl = new VirtualServerStatsProperties();

            vsp2.setConnectionFailures(5L);
            vsp2.setConnectionErrors(6L);
            vsp2.setConnectTimedOut(7L);
            vsp2.setDataTimedOut(8L);
            vsp2.setKeepaliveTimedOut(9L);
            vsp2.setMaxConn(11L); // maxconn is top value from shadow or regular server
            vsp2.setCurrentConn(4L);
            vs2.setStatistics(vsp2);
            virtualServerStatsList.add(vs2);

            vsp2ssl.setConnectionFailures(11L);
            vsp2ssl.setConnectionErrors(12L);
            vsp2ssl.setConnectTimedOut(13L);
            vsp2ssl.setDataTimedOut(14L);
            vsp2ssl.setKeepaliveTimedOut(15L);
            vsp2ssl.setMaxConn(17L); // maxconn is top value from shadow or regular server
            vsp2ssl.setCurrentConn(17L);
            vs2ssl.setStatistics(vsp2ssl);
            virtualServerStatsSslList.add(vs2ssl);


            stats = CustomMappings.mapVirtualServerStatsLists(virtualServerStatsList, virtualServerStatsSslList);
            Assert.assertEquals(10, stats.getConnectFailure());
            Assert.assertEquals(12, stats.getConnectError());
            Assert.assertEquals(14, stats.getConnectTimeOut());
            Assert.assertEquals(16, stats.getDataTimedOut());
            Assert.assertEquals(18, stats.getKeepAliveTimedOut());
            Assert.assertEquals(11, stats.getMaxConn());
            Assert.assertEquals(8, stats.getCurrentConn());

            Assert.assertEquals(22, stats.getConnectFailureSsl());
            Assert.assertEquals(24, stats.getConnectErrorSsl());
            Assert.assertEquals(26, stats.getConnectTimeOutSsl());
            Assert.assertEquals(28, stats.getDataTimedOutSsl());
            Assert.assertEquals(30, stats.getKeepAliveTimedOutSsl());
            Assert.assertEquals(17, stats.getMaxConnSsl());
            Assert.assertEquals(34, stats.getCurrentConnSsl());
        }
    }
}