package org.openstack.atlas.api.validation.verifiers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HostNameVerifierTest {

    private HostNameVerifier hostNameVerifier;

    @Before
    public void standUp() {
        hostNameVerifier = new HostNameVerifier();
    }

    @Test
    public void passCase1() {
        VerifierResult result = hostNameVerifier.verify("hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase2() {
        VerifierResult result = hostNameVerifier.verify("www.hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase3() {
        VerifierResult result = hostNameVerifier.verify("*.hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase4() {
        VerifierResult result = hostNameVerifier.verify("*.*.hostname.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase5() {
        VerifierResult result = hostNameVerifier.verify("*.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase6() {
        VerifierResult result = hostNameVerifier.verify("*.*.com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void passCase7() {
        VerifierResult result = hostNameVerifier.verify("com");
        Assert.assertTrue(result.passed());
    }

    @Test
    public void failCase1() {
        VerifierResult result = hostNameVerifier.verify("");
        Assert.assertFalse(result.passed());
    }

    @Test
    public void failCase2() {
        VerifierResult result = hostNameVerifier.verify("*");
        Assert.assertFalse(result.passed());
    }

    @Test
    public void failCase3() {
        VerifierResult result = hostNameVerifier.verify("**");
        Assert.assertFalse(result.passed());
    }
}
