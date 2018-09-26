package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.api.mgmt.helpers.StubFactory;
import org.openstack.atlas.api.mgmt.validation.validators.HostsValidator;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.openstack.atlas.api.helpers.ResultMessage.resultMessage;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.PUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HostsValidatorTest {

    private HostsValidator hv;
    private Hosts hs;
    private Host h;
    private ValidatorResult res;
    private static Random rnd = new Random();

    @Before
    public void setUp() {
        h = StubFactory.rndHostPut();
        hv = new HostsValidator();
        hs = new Hosts();
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
    public void shouldAcceptMultiHostPut() {
        hs.getHosts().add(StubFactory.rndHostPut());
        hs.getHosts().add(StubFactory.rndHostPut());
        res = hv.validate(hs,PUT);
        assertTrue(resultMessage(res,PUT),res.passedValidation());
    }

}
