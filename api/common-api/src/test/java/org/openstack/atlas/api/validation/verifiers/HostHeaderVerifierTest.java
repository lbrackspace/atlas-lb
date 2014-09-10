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
    public void passCase1() {
        VerifierResult result = hostHeaderVerifier.verify("hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase2() {
        VerifierResult result = hostHeaderVerifier.verify("www.hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void failCase1() {
        VerifierResult result = hostHeaderVerifier.verify("");
        Assert.assertFalse(result.passed());
    }

    @Test
    public void failCase2() {
        VerifierResult result = hostHeaderVerifier.verify("blah blah");
        Assert.assertFalse(result.passed());
    }

    @Test
    public void failCase3() {
        VerifierResult result = hostHeaderVerifier.verify("1.2.3.4");
        Assert.assertFalse(result.passed());
    }
}
