/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.service.domain.services.helpers;

import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.junit.After;

import org.junit.Before;

import org.junit.Test;
import static org.junit.Assert.*;

public class SslTerminationHelperTest {

    private static final Boolean F = Boolean.FALSE;
    private static final Boolean T = Boolean.TRUE;
    private SslTermination apiSslTerm;
    private org.openstack.atlas.service.domain.entities.SslTermination dbSslTerm;

    public SslTerminationHelperTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldAcceptAllCasesWhereRencryptionisTurnedOff() {
        apiSslTerm = newApiSslTerm();
        dbSslTerm = newDbSslTerm();

        apiSslTerm.setReEncryptionEnabled(F);
        assertTrue(isValidRencrypt(apiSslTerm, null));
        assertTrue(isValidRencrypt(apiSslTerm, dbSslTerm));
    }

    public void shouldPukeIfDisableingSecureTrafficOnRencryptedLb() {
        apiSslTerm = newApiSslTerm();
        dbSslTerm = newDbSslTerm();

        apiSslTerm.setSecureTrafficOnly(F);
        dbSslTerm.setSecureTrafficOnly(true);

        assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));
    }

    @Test
    public void shouldPukeIfSettingRencryptionWithNoSecureTrafficOnly() {
        apiSslTerm = newApiSslTerm();
        dbSslTerm = newDbSslTerm();
        apiSslTerm.setReEncryptionEnabled(T);
        assertFalse(isValidRencrypt(apiSslTerm, null));
        assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));
        apiSslTerm.setSecureTrafficOnly(F);
        assertFalse(isValidRencrypt(apiSslTerm, null));
        assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));

        // But its cool if they enable SecureTraffic only at build time
        apiSslTerm.setSecureTrafficOnly(T);
        assertTrue(isValidRencrypt(apiSslTerm, null));
        assertTrue(isValidRencrypt(apiSslTerm, dbSslTerm));
    }

    @Test
    public void shouldBlahTemplate() {
        apiSslTerm = newApiSslTerm();
        dbSslTerm = newDbSslTerm();


    }

    private static SslTermination newApiSslTerm() {
        return new SslTermination();
    }

    private static org.openstack.atlas.service.domain.entities.SslTermination newDbSslTerm() {
        return new org.openstack.atlas.service.domain.entities.SslTermination();
    }

    // I really don't feel like retyping verifyUserIsNotSettingNonSecureTrafficAndReEncryption over and over and over
    private static boolean isValidRencrypt(SslTermination apiSsl, org.openstack.atlas.service.domain.entities.SslTermination dbSsl) {
        return SslTerminationHelper.verifyUserIsNotSettingNonSecureTrafficAndReEncryption(apiSsl, dbSsl);
    }
}
