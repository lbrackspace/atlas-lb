package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Zone;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.mgmt.validation.validators.HostValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//@Ignore
public class HostValidatorTest {

    private HostValidator hv;
    private Host host;
    private ValidatorResult res;
    private static Random rnd = new Random();

    @Before
    public void setUp() {
        host = rndHostPost();
        hv = new HostValidator();
    }

    public static Integer rndPosInt(int lo, int hi) {
        int ri = rnd.nextInt();
        ri = ri < 0 ? 0 - ri : ri;
        return new Integer(ri % (hi - lo + 1) + lo);
    }

    public static String rndIp() {
        String out = String.format("%s.%s.%s.%s", rndPosInt(0, 255), rndPosInt(0, 255), rndPosInt(0, 255), rndPosInt(0, 255));
        return out;
    }

    public static Object rndChoice(List oList) {
        int ri = rndPosInt(0, oList.size() - 1);
        return oList.get(ri);
    }

    public static Object rndChoice(Object[] oArray) {
        int ri = rndPosInt(0, oArray.length - 1);
        return oArray[ri];
    }

    public Host rndHostPost() {
        Host h;
        h = new Host();

        h.setName(String.format("Host.%s", rndPosInt(0, 10000)));
        h.setClusterId(rndPosInt(0, 10000));
        h.setCoreDeviceId(rndPosInt(0, 10000).toString());
        h.setZone(Zone.A);
        h.setMaxConcurrentConnections(rndPosInt(0, 900));
        h.setManagementIp(rndIp());
        h.setManagementInterface(String.format("http://%s:8080/config", rndIp()));
        h.setType((HostType) rndChoice(HostType.values()));
        h.setHostName("someNode.openstackcloud.org");
        h.setEndpointActive(true);
        h.setIpv4Public("10.10.10.100");
        h.setIpv4Servicenet("20.20.20.200");

        return h;
    }

    @Test
    public void shouldAcceptGoodHostPost() {
        res = hv.validate(host, POST);
        assertTrue(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingNameOnPost() {
        host.setName(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingClusterIdOnPost() {
        host.setClusterId(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingCoreIdOnPost() {
        host.setCoreDeviceId(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldAcceptifMissingStatusOnPost() {
        host.setStatus(null);
        res = hv.validate(host, POST);
        assertTrue(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifStatusOnPost() {
        host.setStatus(HostStatus.ACTIVE);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingMaxConnectionsOnPost() {
        host.setMaxConcurrentConnections(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingMIPOnPost() {
        host.setManagementIp(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectifMissingInterfaceOnPost() {
        host.setManagementInterface(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectIdManglingOnPost() {
        host.setId(222);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectIdManglingOnPut() {
        host.setId(222);
        host.setClusterId(null);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldAcceptIdManglingOnLBPut() {
        host.setId(222);
        host.setClusterId(null);
        res = hv.validate(host, LOADBALANCER_PUT);
        assertTrue(resultMessage(res, LOADBALANCER_PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectMissingHostName() {
        host.setHostName(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldRejectMissingEndPointActive() {
        host.setEndpointActive(null);
        res = hv.validate(host, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldAcceptEndPointofFalse() {
        host.setEndpointActive(false);
        res = hv.validate(host, POST);
        assertTrue(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void mustHaveIdOnLBPut() {
        res = hv.validate(host, LOADBALANCER_PUT);
        assertFalse(resultMessage(res, LOADBALANCER_PUT), res.passedValidation());
    }

    @Test
    public void clusterIdShouldNotBeMutableOnPut() {
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldAcceptIdForReassigningHost() {
        host = new Host();
        host.setId(12);
        res = hv.validate(host, ReassignHostContext.REASSIGN_HOST);
        assertTrue(resultMessage(res, ReassignHostContext.REASSIGN_HOST), res.passedValidation());
    }

    @Test
    public void shouldRejectImmutableAttributesForReassigningHost() {
        host = new Host();
        host.setId(23);
        host.setName("name");
        host.setClusterId(2);
        host.setCoreDeviceId("43");
        host.setManagementIp("119.9.9.9");
        host.setMaxConcurrentConnections(34);
        host.setType(HostType.FAILOVER);
        res = hv.validate(host, ReassignHostContext.REASSIGN_HOST);
        assertFalse(resultMessage(res, ReassignHostContext.REASSIGN_HOST), res.passedValidation());
    }

    @Test
    public void shouldRejectBadIpv4Address() {
        host = new Host();
        host.setIpv4Public("www.google.com");
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldAcceptGoodIpv4Address() {
        host = new Host();
        host.setIpv4Public("1.2.3.4");
        res = hv.validate(host, PUT);
        assertTrue(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectBadIpv6Address() {
        host = new Host();
        host.setIpv6Public("www.google.com");
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldAcceptGoodIp6Address() {
        host = new Host();
        host.setIpv6Public("::1");
        res = hv.validate(host, PUT);
        assertTrue(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectTypeOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setType(HostType.ACTIVE);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setUtilization("%99");
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectNumberOfUniqueCustomersOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setNumberOfUniqueCustomers(123);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectNumberOfLoadBalancingConfigurationsOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setNumberOfLoadBalancingConfigurations(1234);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectClusterIdOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setClusterId(1);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldRejectZoneOnPut() {
        host = new Host();
        host.setIpv4Public("100.10.100.10");
        host.setZone(Zone.B);
        res = hv.validate(host, PUT);
        assertFalse(resultMessage(res, PUT), res.passedValidation());
    }
}
