/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.capman.tools.util;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnRSAKeyException;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import static org.junit.Assert.*;
import org.rackspace.capman.tools.ca.CertUtils;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;

public class X509PathBuilderTest {

    private static final String rootKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEowIBAAKCAQEAi2AMd8rFoe3Re9R0ipd2zARspYWqzOwnn2H7eqxGb08zFSQA\n"
            + "RQ/gUtl0MbNocwe063qEpEKJ646BKtaM/3BeH3o2ZBZoUKPl3kaietHuGM9DN9EC\n"
            + "gftl2CeCz8OXfR92vEUhZ/A+lep8NDqnFSXuUhgaZQOuIamrdKqjEB1THZoYNAby\n"
            + "fsdWC1JNv8s/S+IqoHCxuBaezq8CTGtNHjtwRhbSUYKzceu8Tk3ha+/d2YKubVM1\n"
            + "8mdswtNGdQau5kVIi847TaCiBBNGE24Iei9D668nsZaDw+NpvwlJw45cBwgMnYyn\n"
            + "aF6jtgs8tE09BrnTWcRU1DSTyHuecLS62cUwjwIDAQABAoIBAAqzjUGFaDdOs072\n"
            + "uRwLFSwFCvKKmNqzJzetpkl9AMt7IUj2Qq8K0QaLe8h1JgfvB40tExIpqb1Ua8aG\n"
            + "Qr06O3/fOl3k+o12iCfQ9JjJlaaCA389pRul2eQG5JxfQDpzOAKsrCv23ldUccTw\n"
            + "2/Nbvji/cQdgiPY8uNV0ZKDQH5meJeZHuzWIrGZ8dPgoFNs098Li+sEJ3llXCmBr\n"
            + "mMleOkT3EGdFh1KLQuyBld4IVyoQzq4kmD6QpocTM+ne9RBC60TrUhCdoIoaOXeH\n"
            + "T/ur83AqXMqDFxysmpyiud9cTSjE3L50XgeGtTQDFQEEOyGbO6XWAl25upvYU6cb\n"
            + "WlAdoEECgYEA3tBYTn2dfvum3b7B/FTt62a2OKmq4UNZvz/MpqsIlV6NKYTFVUHt\n"
            + "eexnUpSlKsEk4U23CzJwv5KcHy2F00s2DP04W8cEpR9OhBKRV2xQ0CsQ+CTTFsUh\n"
            + "TZf+khSTEzIpbQSJzKNvAtZjmrXEjmf64m79WO7+03V9yqvOe40zFlUCgYEAoCJG\n"
            + "x/WR7rWuHFXXWmcxJS9qAgJiAPE29IbSRvXk7xd+zevGzOLwjN/W+K3elNrpXNig\n"
            + "UakgVoMniRtm3UX/llM8Ysj3GJVDjwou7fOfAZWWm5AGf1JpA6tzZCSyM9EalAh4\n"
            + "p7JhWLJ2vd5Rhtg3JPVGXPoqVqD8OYv4fWSWJ1MCgYB9MQUc/Pl8OrtURnVKaRHR\n"
            + "PUHPbo2Dykrn6Vn8n4bQHnMkS+Rwdf2PjuOzA7AV6LXnHbpmQS4WhbLQ1cwmn1C8\n"
            + "VZ7P+m/Cs2dzT9d4DnUNsdT0CATO+24t4eP4gjTtCTc7eNxQLdgW+Qy4Bb9t4ECX\n"
            + "75LL4GUvvQkz0frPctOsjQKBgQCdx+gyEfcOW5kIdlRQ0SiNWSB8hliGUSYy3bzH\n"
            + "CnXeVyosP8qMCne2dr9cHAUaanBReWjNzXT9iREleQhrshLZzX3MX/OIF1qP0BrZ\n"
            + "fpgHeZsCAHFGdq7eXMq/u6CSidBEV+x6X7qnKIDzJ+Dwmo4LLll8N0oMPWcIVLAs\n"
            + "7TU93QKBgH0F0U5wj1xiI/uOBGQAAD14jUfPRKXNpcPv7Vr2CVrBjuwGai/HVauY\n"
            + "Boods91VGHE9T5t2COxBtUDfyNU+RCzLI59he693VzUgfaDL6pm7U8hFAok2rDuN\n"
            + "6Xoig390LnodozdBpfp5bl1psREQ9Gs5aZDvkonN8WsMSrnLZezf\n"
            + "-----END RSA PRIVATE KEY-----\n"
            + "";
    private static final String rootCrtPem = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDyTCCArGgAwIBAgIBATANBgkqhkiG9w0BAQUFADCBjTEeMBwGA1UEAxMVVGVz\n"
            + "dCBSb290IENBIDIwMTItMS0xMRwwGgYDVQQLExNQbGF0Zm9ybSBDbG91ZFNpdGVz\n"
            + "MRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9u\n"
            + "aW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVUzAeFw0xMjA2MDEyMDM0NDZa\n"
            + "Fw0xNjA1MzEyMDM0NDZaMIGNMR4wHAYDVQQDExVUZXN0IFJvb3QgQ0EgMjAxMi0x\n"
            + "LTExHDAaBgNVBAsTE1BsYXRmb3JtIENsb3VkU2l0ZXMxGjAYBgNVBAoTEVJhY2tz\n"
            + "cGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
            + "YXMxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n"
            + "i2AMd8rFoe3Re9R0ipd2zARspYWqzOwnn2H7eqxGb08zFSQARQ/gUtl0MbNocwe0\n"
            + "63qEpEKJ646BKtaM/3BeH3o2ZBZoUKPl3kaietHuGM9DN9ECgftl2CeCz8OXfR92\n"
            + "vEUhZ/A+lep8NDqnFSXuUhgaZQOuIamrdKqjEB1THZoYNAbyfsdWC1JNv8s/S+Iq\n"
            + "oHCxuBaezq8CTGtNHjtwRhbSUYKzceu8Tk3ha+/d2YKubVM18mdswtNGdQau5kVI\n"
            + "i847TaCiBBNGE24Iei9D668nsZaDw+NpvwlJw45cBwgMnYynaF6jtgs8tE09BrnT\n"
            + "WcRU1DSTyHuecLS62cUwjwIDAQABozIwMDAPBgNVHRMBAf8EBTADAQH/MB0GA1Ud\n"
            + "DgQWBBSk4Rk04si6m6bC0LPaIQCefbFE/DANBgkqhkiG9w0BAQUFAAOCAQEAYjcx\n"
            + "C28uMjWv5vguUGSr1KplxYBxecQsTtgy09gjdkpjcgznJk5yw7kri8iA/LNIzdnv\n"
            + "frn4ObhZrufgVoYcntAn7qA/EXrn4nnTqBzNbdclaOFKcyEjBjbdfsG0bqC0619J\n"
            + "EvXFvn6qTslOF5Z8icdWNBiJYNCeLAzfwsIvSWZC46bc7TuGhVpjoif80sgv48nV\n"
            + "DXYEevYDu2P0SQ2wAKZ0TWA4EWhI5Tra5ttpora2Bj8SFVKbz2sgr+7XLLVPJOkV\n"
            + "XJZb1VPAPkNFNHy0BfMlYWIFjNMSpOoWnOAdUHQGF7qFWIRK/bzH9xiAJcgyHglH\n"
            + "KtepHcu67ShpYsmATw==\n"
            + "-----END CERTIFICATE-----\n"
            + "";
    private static final String rootSubj = "CN=Test Root CA 2012-1-1, "
            + "OU=Platform CloudSites, O=Rackspace Hosting, L=San Antonio, "
            + "ST=Texas, C=US";
    private X509CertificateObject rootCrt;
    private KeyPair rootKey;

    public X509PathBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws NotAnX509CertificateException, NotAnRSAKeyException {
        rootCrt = getCrt(rootCrtPem);
        rootKey = getKey(rootKeyPem);
    }

    @After
    public void tearDown() {
    }

    private X509CertificateObject getCrt(String crtPem) throws NotAnX509CertificateException {
        Object obj;
        NotAnX509CertificateException notCrtException = new NotAnX509CertificateException(crtPem);
        try {
            obj = PemUtils.fromPemString(crtPem);
        } catch (PemException ex) {
            throw notCrtException;
        }
        if (!(obj instanceof X509CertificateObject)) {
            throw notCrtException;
        }
        return (X509CertificateObject) obj;
    }

    private KeyPair getKey(String keyPem) throws NotAnRSAKeyException {
        Object obj;
        NotAnRSAKeyException notKeyException = new NotAnRSAKeyException(keyPem);
        try {
            obj = PemUtils.fromPemString(keyPem);
        } catch (PemException ex) {
            throw notKeyException;
        }
        if (!(obj instanceof KeyPair)) {
            throw notKeyException;
        }
        return (KeyPair) obj;
    }

    @Test
    public void testCreateChainBasedOnRootCa() throws NotAnX509CertificateException, RsaException {
        List<String> subjNames = new ArrayList<String>();
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites, CN=IMD 1");
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites, CN=IMD 2");
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites, CN=IMD 3");
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites, CN=IMD 4");
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites, CN=IMD 5");
        subjNames.add("O=Rackspace hosting, OU=Platform CloudSites,C=US,ST=Texas,L=San Antonio,CN=www.junit-mosso-apache2zeus-test.com");
        long now = System.currentTimeMillis();
        long hourAgo = 1000*60*60;
        long yearFromNow = 1000*60*60*24*365;
        int keySize = 2048;
        Date before = new Date(now - hourAgo);
        Date after = new Date(now + yearFromNow);
        List<X509ChainEntry> chain = X509PathBuilder.newChain(rootKey, rootCrt, subjNames, keySize, before, after);
        assertTrue(chain.size() == 6);
        for(int i=0;i<chain.size();i++){
            X509Certificate x509 = (X509Certificate)chain.get(i).getX509obj();
            String subj = x509.getSubjectX500Principal().getName();
            String expSubj = new X509Principal(subjNames.get(i)).getName();
            assertEquals(expSubj,subj);
        }

        // Verify root signatures
        assertTrue(CertUtils.isSelfSigned(rootCrt));
        assertTrue(CertUtils.validateKeyMatchesCrt(rootKey, rootCrt).isEmpty());
        assertTrue(CertUtils.validateKeySignsCert(rootKey, rootCrt).isEmpty());
        assertTrue(CertUtils.verifyIssuerAndSubjectCert(rootCrt, chain.get(0).getX509obj()).isEmpty());

        for(int i = 1; i< chain.size();i++){
            X509CertificateObject issuerCrt = chain.get(i - 1).getX509obj();
            X509CertificateObject subjectCrt = chain.get(i).getX509obj();
            KeyPair issuerKey = chain.get(i - 1).getKey();
            KeyPair subjectKey = chain.get(i).getKey();

            // Verify subjKey matches Certs pubKey
            assertTrue(CertUtils.validateKeyMatchesCrt(subjectKey, subjectCrt).isEmpty());

            // Verify the isser's key actually signed the subjectCrt
            assertTrue(CertUtils.validateKeySignsCert(issuerKey,subjectCrt).isEmpty());

            // Verify the negative case that the subjCert didn't sign the root CA or something
            assertFalse(CertUtils.validateKeySignsCert(issuerKey,rootCrt).isEmpty());

            // Verify the issuer cert signed the subject crt;
            assertTrue(CertUtils.verifyIssuerAndSubjectCert(issuerCrt, subjectCrt).isEmpty());

            // Verify the subject cert did not sign the issuer cert.
            assertFalse(CertUtils.verifyIssuerAndSubjectCert(subjectCrt, issuerCrt).isEmpty());

            StringBuilder pemChain = new StringBuilder(4096);
            for(X509ChainEntry entry : chain){
                pemChain.append(PemUtils.toPemString(entry.getX509obj()));
            }
            // The users Site cert is at the end of the chain
            X509ChainEntry userEntry = chain.get(chain.size() - 1);
            KeyPair userKey = userEntry.getKey();
            X509CertificateObject userCrt = userEntry.getX509obj();
            PKCS10CertificationRequest userCsr = userEntry.getCsr();
            System.out.printf("chain:\n%s\n\n",pemChain.toString());
            System.out.printf("RootCa:\n%s\n\n", PemUtils.toPemString(rootCrt));
            System.out.printf("RootKey:\n%s\n\n", PemUtils.toPemString(rootKey));
            System.out.printf("userKey:\n%s\n\n",PemUtils.toPemString(userKey));
            System.out.printf("userCrt:\n%s\n\n",PemUtils.toPemString(userCrt));
            System.out.printf("userCsr:\n%s\n\n",PemUtils.toPemString(userCsr));
        }
    }
}
