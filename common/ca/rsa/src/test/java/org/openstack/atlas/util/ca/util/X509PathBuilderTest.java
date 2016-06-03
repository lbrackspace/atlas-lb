/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.ca.util;

import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
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
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnRSAKeyException;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import static org.junit.Assert.*;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.RsaException;

public class X509PathBuilderTest {

    private static final String rootKeyPem = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIJKQIBAAKCAgEAmER5iviUcEGRtLZHts2/BjAbJ4gBihJX5vz/CTShPT7uK0De\n"
            + "OcetcgAA3NdTZ6EmIjAK8Vp0r/IkcAjsRjOhGN4hzvBlkkf0pJCUlVcGznO6eeOj\n"
            + "USID3heUidvRxWLYZcs8/7Ei6ec9n4yQd672clEpvbjuFEdgDrllS7zbiPk4jAZ7\n"
            + "lGWvYQKr6h8GzcMDa23vKx/0ztDYF/f8jNmWPE5GWKTyeNvA0pL/YBN1Uh3U1UoZ\n"
            + "RN6JogZEk0NJPgc+DZV56Q9iRWDAI4hlG9zshqD6LL2MWmhgu8kPifMV5w+Hy2Do\n"
            + "BB049YYejYbDRFNQFxhYvUSjlcVxeSh68sVgWi3m1uNJ/xGTwK8RzlIkEIQChsS2\n"
            + "Y24BDCchDxnNmHuG6yAEIiHEoG7meucZlAGaZ1kGck+gUjbP4m/I8MmLBF6Zuk/f\n"
            + "K730D4iekDdJkkVhbxeZUQG9myaMII46kGcCg1pattl5TBeVZ2WjBocjxBark1en\n"
            + "AtYhHIaHcIvfaW+1oqMnjHzdJEkwCrToTlmZf+e1GyuzSRLEVe1B+gD/j8PKMjnX\n"
            + "0paFWoWuewfE++1POuYqbtQE9UWSjcvBHk1h5iBFhkYeB26ZSk7XCvURrNlI4LH7\n"
            + "EBalu+RYbyUsRCKecQXL6N4WtpG7P4+0lbZCSf8GsLzR5ekDLR5423av4ZMCAwEA\n"
            + "AQKCAgA3CeFJG2tEXF8XjB/F8v7OdMsL8fxkdRby9pYVNMHniny6g7sP9Z47K8ck\n"
            + "DG0oczZ+exEphoHRK1yCZqdJBNaBT6G21GxSgNHsPhqHFKrwKyLHdkINTJdLSwi1\n"
            + "ABEGISGJQfptjhDJrX03065QpJyOW8oTuunLyLTu/ZKg2sYb78HB04IN9Od80GNl\n"
            + "wAtMiuYMifM+ilGKBDFoEIpoOyoz6DNDTXQbvYsoUlwe1Um+AexxoFQb8bicKVnW\n"
            + "RScPVbzXWiVz2cyyuTMaZO3vMV8Jltx3GZjHAR2eIyILwiMznwh9uiyDeDCjhvQ6\n"
            + "QfEJHHhBsNo6p7ndpD3NBpXSdQfLYiortK0ypmig7S5HSdUnVFRaUhkGsi2SBI5t\n"
            + "FoqZ0MhxCsiR0hCM+HtQfpHbUpJMirf+rAwBAXIG2DyN2TiSGGNJfWyozV095E5e\n"
            + "Ytd9TP3IJ2RubQjz+4eDbx40DPdbaCBUyCIb6u1a+mDozwOYM+MX5agQZBtGNV2J\n"
            + "YnZrKwoMVe+qkTk+uES26DHWMr8Sa2DC00wFkBQkGQZ6oJl6QoL3GlDHKq/5bTlH\n"
            + "E076tpNwGe6/FhbBmhTWotwF6uxAEJfqNPECc+gF+gceAsASIQR5DSeiG4zXkXUx\n"
            + "Y2oLeL79D088Gx0+kg0Sl8BZVO78n1RXR+JhWAwoOssdpwTi0QKCAQEAy3zNlFfv\n"
            + "CgXFNbKNOp5yIgFVGWDiZpeTrWz20mzIdZdCPVUsqWNaRvRNp/iCU8RGKKki4nwQ\n"
            + "HM9AEKF5GtTpz9QyKyEUKN9XQLL4lfpnb1dgcWVsK1sd0hYIVozNW9yNr8NVs+RG\n"
            + "9cm7Ks8V7pJm25VT+M1VS0CpxP2HAN0L3k/myVqMuPzCRdJHqBzXPcLUpyyjoaRZ\n"
            + "j1a6H5ILafsprrEe/uFiUmDv4NL92YdVV9BeyZRyHcW+NbgbrRWbpRh65MWhmMb4\n"
            + "j74F8lcJZ02AfsdMYCq3zYGejwFY9gTB3oF43oSROZKLpM5OmdlaqmODb4VQrH/M\n"
            + "dtlwo6o/K0YBSQKCAQEAv4/fPVIkOH7DSsg8f3H7LL3qTsgZvHx6PEuFr61R+DsB\n"
            + "hQajT5Hh2Q3SD5YxeoI5pPEAUiDdRSoCjy+HRKZa3mx22x1noJm0UdwQ/3gqmpzB\n"
            + "tCBQacMwEU2XapfSjKE6SKGi0y6jBH7kZSXtosESzMIQzp/cjL/7RSrIl+ddEN71\n"
            + "l1bratTPOx/ImETUw57ZvNcz0CQ3K+suO4U0vwXASTzkiBHcWYx+pSF4T6/d1Sf8\n"
            + "+02J74jz21a51htMHdRZmPpQUwEmvlK8hUBHTTpQPdylj2/kZ0gRNHrFpWWHnh4C\n"
            + "Rq4F7ewkcvcHGu9PANgMP+SNddTsQi0Eu97ZIoan+wKCAQEAsF9IQN/GzMUbneNZ\n"
            + "DgnvmxHUlN6o3Ytb8OGVPeYUCfhE7aw5vcfjo8R02rZHIBvrQ6r2FaC6xn3MZZ3P\n"
            + "vjX1QrIFmeRd+N88XVPCqxwTMypk6WSjUSD0w/dc2sfH52tPU5zXV8jwyHFFzWEY\n"
            + "/WPfy0uvMOOonkU54QhuT41IkDRpMPVPK7fJG4OboFb/KQUKFKxYV+5/wgHF6T9I\n"
            + "rV+JTWUwHHN2KTjkMdYvOLBYhHiS792+25ddumoNcwRbl8mUG15BJo6vOsfVl6tg\n"
            + "9yzxxvEvfdsI3RMQP5tHZFXTQ+ysuxHkXiZq6zn2XttJ/o2qiqreteU/b3QgcXtK\n"
            + "rVNMIQKCAQEAvon5kAH7OyKDWAfxfQa/wpIo1DHev4fFAeJQ2Bv6k4EFku62VDgT\n"
            + "wBUqIwzOrD+J3NFG82nzDY+qugeZcaRScDjMxEfsQmeAX4gYdDNvlRhk56jYs0pN\n"
            + "rnhSKtlWuUDJO2NjFNGnnpokzaM7So7nBznlA9Eoy+tNoE/c9JNSE6zh9aWkRj/k\n"
            + "3LdRjHuycd24UEhyJg9PvWSROOzXOUYcK/zgh6PUXFINymyPQhv498Nts/09PUAo\n"
            + "5rvN3vSJ8oW8lRgt+1IC2n5rO9Ni2KMvG0k0eIbgVgbt7hhMLabejVzmAK2qAizH\n"
            + "WH7z10u/dmRvUsIgHtsqIOysb75KljgALwKCAQAikkinBQxOqqN3FL8xp7+OYwFt\n"
            + "JIzAoxWxaPToFiA+3WtU0MhfCfzhiiLa/83KU2Z6XW+jdlD32Nc85Onw0Wq5ZN0q\n"
            + "ZDG+vrCH8xRWwMEJ9jIhdKSo4CMhBFTorhi157/gafNK/GyWLOZdlilBfoN9EFTN\n"
            + "kPLmegljCf4n9NlKfrtEddVAkyarWgQE3evPUvJbusMAfOSt7eL2XbfY9V7vjopj\n"
            + "/RZ4pBWqg11Tz2+98QMJ30cWUujvyy047kwrftOwnwUxFlcNa2Vy+l7iYrpYLQEZ\n"
            + "f8KOEdEQXEJIl8J5qGRpAfW/nMECcN7I9n30dTN+2xxLqfsglLlcSzaxkYLF\n"
            + "-----END RSA PRIVATE KEY-----\n";

    private static final String rootCrtPem = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIFmTCCA4GgAwIBAgIBATANBgkqhkiG9w0BAQsFADB2MRAwDgYDVQQDEwdUZXN0\n"
            + "IENBMRswGQYDVQQLExJDbG91ZExvYWRCYWxhbmNlcnMxEjAQBgNVBAoTCVJhY2tz\n"
            + "cGFjZTEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYD\n"
            + "VQQGEwJVUzAeFw0xNjA2MDMyMzA3NTdaFw0zMjA1MzAyMzA3NTdaMHYxEDAOBgNV\n"
            + "BAMTB1Rlc3QgQ0ExGzAZBgNVBAsTEkNsb3VkTG9hZEJhbGFuY2VyczESMBAGA1UE\n"
            + "ChMJUmFja3NwYWNlMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
            + "YXMxCzAJBgNVBAYTAlVTMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA\n"
            + "mER5iviUcEGRtLZHts2/BjAbJ4gBihJX5vz/CTShPT7uK0DeOcetcgAA3NdTZ6Em\n"
            + "IjAK8Vp0r/IkcAjsRjOhGN4hzvBlkkf0pJCUlVcGznO6eeOjUSID3heUidvRxWLY\n"
            + "Zcs8/7Ei6ec9n4yQd672clEpvbjuFEdgDrllS7zbiPk4jAZ7lGWvYQKr6h8GzcMD\n"
            + "a23vKx/0ztDYF/f8jNmWPE5GWKTyeNvA0pL/YBN1Uh3U1UoZRN6JogZEk0NJPgc+\n"
            + "DZV56Q9iRWDAI4hlG9zshqD6LL2MWmhgu8kPifMV5w+Hy2DoBB049YYejYbDRFNQ\n"
            + "FxhYvUSjlcVxeSh68sVgWi3m1uNJ/xGTwK8RzlIkEIQChsS2Y24BDCchDxnNmHuG\n"
            + "6yAEIiHEoG7meucZlAGaZ1kGck+gUjbP4m/I8MmLBF6Zuk/fK730D4iekDdJkkVh\n"
            + "bxeZUQG9myaMII46kGcCg1pattl5TBeVZ2WjBocjxBark1enAtYhHIaHcIvfaW+1\n"
            + "oqMnjHzdJEkwCrToTlmZf+e1GyuzSRLEVe1B+gD/j8PKMjnX0paFWoWuewfE++1P\n"
            + "OuYqbtQE9UWSjcvBHk1h5iBFhkYeB26ZSk7XCvURrNlI4LH7EBalu+RYbyUsRCKe\n"
            + "cQXL6N4WtpG7P4+0lbZCSf8GsLzR5ekDLR5423av4ZMCAwEAAaMyMDAwDwYDVR0T\n"
            + "AQH/BAUwAwEB/zAdBgNVHQ4EFgQU2MnMNaImho87jQe39eqppwogZDIwDQYJKoZI\n"
            + "hvcNAQELBQADggIBADmmqgjwLXz+piIaFW/5iQa3Uf9+7tLXXbIbqTYtekcFf6X8\n"
            + "W7e3Mtb6dk8DhnCLejnNO09NP4ysf1MH5As0Zx8rlDpYqS3fxsGMYHAat+NxjNdw\n"
            + "OTCFDL07C2jUZqOBc4tZN7F1CS0tlL33Nm3Bh0HjYYmbDF/wTud9xFdmftGhc/oS\n"
            + "vRlunEs8aWgFQgKzayNcEy9Sr7bR8gbYVLFcxktt324HEItKtUymIg0hVkN8Pulj\n"
            + "ktMD1ByuJM3HWGKyIH+ZXbCxKxNwM4pCkozAqFD7UUXWUKp81zlBxVSxsKKpp6rf\n"
            + "5+zLFvZ2YqSgHxJcdYTdNit8lfsBmS74jwTjssaUuCKFarzG6ra1wuJpsVxuIZ70\n"
            + "kbbQ8DIRkKreHAwk4dO5UINtzCLsWRfkDWQcaLNOgI3HxWgWXow0I8s/6mhplhxs\n"
            + "wF+jArBFU0qXazZryjlSWQEGYt2GKyYFkS6W/EHhS0inzub5pZyea69Xqu53oGUE\n"
            + "VA9FKb+qmwof6We9eCTtI47vt+axMO83qF+Iukp0iRjFusAeWK1g9lKH6dxbndNX\n"
            + "gXGPWKzhrks+hQs6oVNeWEoCZ4XgphevNLXkny+N0LQ+8XArIRxCsQ3lHVHEQgN1\n"
            + "awH1Ujz1Gj+f5H5dxxhyEl+nnAga2jAmrfk1oGglPA6upX7akFaECVDIKfrM\n"
            + "-----END CERTIFICATE-----\n"
            + "";
    private static final String rootSubj = "CN=Test CA, OU=CloudLoadBalancers, O=Rackspace, L=San Antonio, ST=Texas, C=US";
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
        long hourAgo = 1000 * 60 * 60;
        long yearFromNow = 1000 * 60 * 60 * 24 * 365;
        int keySize = 2048;
        Date before = new Date(now - hourAgo);
        Date after = new Date(now + yearFromNow);
        List<X509ChainEntry> chain = X509PathBuilder.newChain(rootKey, rootCrt, subjNames, keySize, before, after);
        assertTrue(chain.size() == 6);
        for (int i = 0; i < chain.size(); i++) {
            X509Certificate x509 = (X509Certificate) chain.get(i).getX509obj();
            String subj = x509.getSubjectX500Principal().getName();
            String expSubj = new X509Principal(subjNames.get(i)).getName();
            assertEquals(expSubj, subj);
        }

        // Verify root signatures
        assertTrue(CertUtils.isSelfSigned(rootCrt));
        assertTrue(CertUtils.validateKeyMatchesCrt(rootKey, rootCrt).isEmpty());
        assertTrue(CertUtils.validateKeySignsCert(rootKey, rootCrt).isEmpty());
        assertTrue(CertUtils.verifyIssuerAndSubjectCert(rootCrt, chain.get(0).getX509obj()).isEmpty());

        for (int i = 1; i < chain.size(); i++) {
            X509CertificateObject issuerCrt = chain.get(i - 1).getX509obj();
            X509CertificateObject subjectCrt = chain.get(i).getX509obj();
            KeyPair issuerKey = chain.get(i - 1).getKey();
            KeyPair subjectKey = chain.get(i).getKey();

            // Verify subjKey matches Certs pubKey
            assertTrue(CertUtils.validateKeyMatchesCrt(subjectKey, subjectCrt).isEmpty());

            // Verify the isser's key actually signed the subjectCrt
            assertTrue(CertUtils.validateKeySignsCert(issuerKey, subjectCrt).isEmpty());

            // Verify the negative case that the subjCert didn't sign the root CA or something
            assertFalse(CertUtils.validateKeySignsCert(issuerKey, rootCrt).isEmpty());

            // Verify the issuer cert signed the subject crt;
            assertTrue(CertUtils.verifyIssuerAndSubjectCert(issuerCrt, subjectCrt).isEmpty());

            // Verify the subject cert did not sign the issuer cert.
            assertFalse(CertUtils.verifyIssuerAndSubjectCert(subjectCrt, issuerCrt).isEmpty());

            StringBuilder pemChain = new StringBuilder(4096);
            for (X509ChainEntry entry : chain) {
                pemChain.append(PemUtils.toPemString(entry.getX509obj()));
            }
            // The users Site cert is at the end of the chain
            X509ChainEntry userEntry = chain.get(chain.size() - 1);
            KeyPair userKey = userEntry.getKey();
            X509CertificateObject userCrt = userEntry.getX509obj();
            PKCS10CertificationRequest userCsr = userEntry.getCsr();
            System.out.printf("chain:\n%s\n\n", pemChain.toString());
            System.out.printf("RootCa:\n%s\n\n", PemUtils.toPemString(rootCrt));
            System.out.printf("RootKey:\n%s\n\n", PemUtils.toPemString(rootKey));
            System.out.printf("userKey:\n%s\n\n", PemUtils.toPemString(userKey));
            System.out.printf("userCrt:\n%s\n\n", PemUtils.toPemString(userCrt));
            System.out.printf("userCsr:\n%s\n\n", PemUtils.toPemString(userCsr));
        }
    }
}
