package org.openstack.atlas.api.mgmt.validation.validators;

import org.junit.Assert;
import org.mockito.Mock;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.api.mgmt.helpers.StubFactory;
import org.openstack.atlas.api.mgmt.validation.validators.HostsValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.PUT;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.POST;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HostsValidatorTest {

    private HostsValidator hv;
    private Hosts hs;
    private Host h;
    private Host hPost;
    private ValidatorResult res;
    private static Random rnd = new Random();

    @Before
    public void setUp() {
        h = StubFactory.rndHostPut();
        hv = new HostsValidator();
        hs = new Hosts();
        hPost = new Host();
        hPost.setIpv4Public("172.11.11.110");
        hPost.setIpv4Servicenet("10.2.2.80");
        hPost.setRestEndpointActive(true);
        hPost.setSoapEndpointActive(true);
        hPost.setZone(Zone.B);
        hPost.setTrafficManagerName("zues01.bilip.blah");
        hPost.setName("Test");
        hPost.setMaxConcurrentConnections(5);
        hPost.setManagementRestInterface("https://SomeRestNode.com:9070/api/tm/2.0/config/active/");
        hPost.setManagementSoapInterface("https://SomeSoapNode.com:9090");
        hPost.setManagementIp("12.34.56.78");
        hPost.setCoreDeviceId("SomeCoreDevice");
        hPost.setClusterId(1);
    }


    @Test
    public void shouldRejectEmptyHostsListOnPut() {
        res = hv.validate(hs,PUT);
        assertFalse(resultMessage(res,PUT),res.passedValidation());
    }

    @Test
    public void shouldAcceptSingleHostOnPut() {
        h = new Host();
        h.setName("Blah");
        hs.getHosts().add(h);
        res = hv.validate(hs,PUT);
        assertTrue(resultMessage(res,PUT),res.passedValidation());
    }

    @Test
    public void shouldRejectHostTypeOnPost(){
        hPost.setType(HostType.FAILOVER);
        hs.getHosts().add(hPost);
        res = hv.validate(hs, POST);
        assertFalse(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldAcceptHostTypeNullOnPost(){
        hs.getHosts().add(hPost);
        res = hv.validate(hs, POST);
        Assert.assertNull(h.getType());
        assertTrue(resultMessage(res, POST), res.passedValidation());
    }

    @Test
    public void shouldAcceptValidStatusOnPut(){
        h = new Host();
        h.setStatus(HostStatus.ACTIVE_TARGET);
        hs.getHosts().add(h);
        res = hv.validate(hs, PUT);
        assertTrue(resultMessage(res, PUT), res.passedValidation());
    }

    @Test
    public void shouldAcceptMultiHostPut() {
        hs.getHosts().add(StubFactory.rndHostPut());
        hs.getHosts().add(StubFactory.rndHostPut());
        res = hv.validate(hs,PUT);
        assertTrue(resultMessage(res,PUT),res.passedValidation());
    }

}
