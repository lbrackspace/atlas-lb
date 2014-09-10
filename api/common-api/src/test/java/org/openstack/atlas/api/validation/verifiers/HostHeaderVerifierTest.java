package org.openstack.atlas.api.validation.verifiers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HostHeaderVerifierTest {

    private HostHeaderVerifier hostHeaderVerifier;

    @Before
    public void standUp() {
        hostHeaderVerifier = new HostHeaderVerifier();
    }

    @Test
    public void case1() {
        VerifierResult result = hostHeaderVerifier.verify("hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void case2() {
        VerifierResult result = hostHeaderVerifier.verify("www.hostname.com");
        Assert.assertTrue(result.passed());
    }
}
