package org.openstack.atlas.service.domain.services;

import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import org.openstack.atlas.service.domain.services.impl.SslTerminationServiceImpl;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;

import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@RunWith(Enclosed.class)
public class SslTerminationServiceImplTest {

    public static class whenUpdatingSslTermination {


        @Mock
        RestApiConfiguration restApiConfiguration;

        @Mock
        LoadBalancerRepository loadBalancerRepository;

        @Mock
        SslTerminationRepository sslTerminationRepository;

        @Mock
        VirtualIpRepository virtualIpRepository;
        @Mock
        SslCipherProfileService sslCipherProfileService;


        @InjectMocks
        SslTerminationServiceImpl sslTerminationService;

        private Response response;
        private static KeyPair userKey;
        private static X509CertificateHolder userCrt;
        private static Set<X509CertificateHolder> imdCrts;
        private static X509CertificateHolder rootCA;
        private static final int keySize = 512; // Keeping the key small for testing
        private static List<X509ChainEntry> chainEntries;
        // These are for testing pre defined keys and certs
        private static String workingRootCa;
        private static String workingUserKey;
        private static String workingUserCrt;
        private static String workingUserChain;


        LoadBalancer loadBalancer;
        org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination sslTermination;
        SslTermination sslTerminationToBeUpdated;
        String iv;

        @BeforeClass
        public static void setUpClass() throws RsaException, NotAnX509CertificateException {
            List<X509CertificateHolder> orderImds = new ArrayList<X509CertificateHolder>();
            long now = System.currentTimeMillis();
            long lastYear = now - (long) 1000 * 24 * 60 * 60 * 365;
            long nextYear = now + (long) 1000 * 24 * 60 * 60 * 365;
            Date notBefore = new Date(lastYear);
            Date notAfter = new Date(nextYear);
            String wtf = String.format("%s\n%s", StaticHelpers.getDateString(notBefore), StaticHelpers.getDateString(notAfter));
            List<String> subjNames = new ArrayList<String>();
            // Root SubjName
            subjNames.add("CN=RootCA");

            // Add the middle subjs
            for (int i = 1; i <= 7; i++) {
                String fmt = "CN=Intermedite Cert %s";
                String subjName = String.format(fmt, i);
                subjNames.add(subjName);
            }

            // Lastly add the end user subj
            String subjName = "CN=www.rackexp.org";
            subjNames.add(subjName);
            chainEntries = X509PathBuilder.newChain(subjNames, keySize, notBefore, notAfter);
            int lastIdx = chainEntries.size() - 1;
            rootCA = chainEntries.get(0).getX509Holder();
            userCrt = chainEntries.get(lastIdx).getX509Holder();
            userKey = chainEntries.get(lastIdx).getKey();

            imdCrts = new HashSet<X509CertificateHolder>();
            for (int i = 1; i < lastIdx; i++) {
                imdCrts.add(chainEntries.get(i).getX509Holder());
                orderImds.add(chainEntries.get(i).getX509Holder());
            }

            workingRootCa = PemUtils.toPemString(rootCA);
            workingUserCrt = PemUtils.toPemString(userCrt);
            workingUserKey = PemUtils.toPemString(userKey);
            Collections.reverse(orderImds);
            StringBuilder sb = new StringBuilder();
            for (X509CertificateHolder imd : orderImds) {
                sb = sb.append(PemUtils.toPemString(imd)).append("\n");
            }
            workingUserChain = sb.toString();
        }


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadBalancer = new LoadBalancer();
//            privateKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEAwIudSMpRZx7TS0/AtDVX3DgXwLD9g+XrNaoazlhwhpYALgzJ\nLAbAnOxT6OT0gTpkPus/B7QhW6y6Auf2cdBeW31XoIwPsSoyNhxgErGBxzNARRB9\nlI1HCa1ojFrcULluj4W6rpaOycI5soDBJiJHin/hbZBPZq6vhPCuNP7Ya48Zd/2X\nCQ9ft3XKfmbs1SdrdROIhigse/SGRbMrCorn/vhNIuohr7yOlHG3GcVcUI9k6ZSZ\nBbqF+ZA4ApSF/Q6/cumieEgofhkYbx5fg02s9Jwr4IWnIR2bSHs7UQ6sVgKYzjs7\nPd3Unpa74jFw6/H6shABoO2CIYLotGmQbFgnpwIDAQABAoIBAQCBCQ+PCIclJHNV\ntUzfeCA5ZR4F9JbxHdRTUnxEbOB8UWotckQfTScoAvj4yvdQ42DrCZxj/UOdvFOs\nPufZvlp91bIz1alugWjE+p8n5+2hIaegoTyHoWZKBfxak0myj5KYfHZvKlbmv1ML\nXV4TwEVRfAIG+v87QTY/UUxuF5vR+BpKIbgUJLfPUFFvJUdl84qsJ44pToxaYUd/\nh5YAGC00U4ay1KVSAUnTkkPNZ0lPG/rWU6w6WcTvNRLMd8DzFLTKLOgQfHhbExAF\n+sXPWjWSzbBRP1O7fHqq96QQh4VFiY/7w9W+sDKQyV6Ul17OSXs6aZ4f+lq4rJTI\n1FG96YiBAoGBAO1tiH0h1oWDBYfJB3KJJ6CQQsDGwtHo/DEgznFVP4XwEVbZ98Ha\nBfBCn3sAybbaikyCV1Hwj7kfHMZPDHbrcUSFX7quu/2zPK+wO3lZKXSyu4YsguSa\nRedInN33PpdnlPhLyQdWSuD5sVHJDF6xn22vlyxeILH3ooLg2WOFMPmVAoGBAM+b\nUG/a7iyfpAQKYyuFAsXz6SeFaDY+ZYeX45L112H8Pu+Ie/qzon+bzLB9FIH8GP6+\nQpQgmm/p37U2gD1zChUv7iW6OfQBKk9rWvMpfRF6d7YHquElejhizfTZ+ntBV/VY\ndOYEczxhrdW7keLpatYaaWUy/VboRZmlz/9JGqVLAoGAHfqNmFc0cgk4IowEj7a3\ntTNh6ltub/i+FynwRykfazcDyXaeLPDtfQe8gVh5H8h6W+y9P9BjJVnDVVrX1RAn\nbiJ1EupLPF5sVDapW8ohTOXgfbGTGXBNUUW+4Nv+IDno+mz/RhjkPYHpnM0I7c/5\ntGzOZsC/2hjNgT8I0+MWav0CgYEAuULdJeQVlKalI6HtW2Gn1uRRVJ49H+LQkY6e\nW3+cw2jo9LI0CMWSphNvNrN3wIMp/vHj0fHCP0pSApDvIWbuQXfzKaGko7UCf7rK\nf6GvZRCHkV4IREBAb97j8bMvThxClMNqFfU0rFZyXP+0MOyhFQyertswrgQ6T+Fi\n2mnvKD8CgYAmJHP3NTDRMoMRyAzonJ6nEaGUbAgNmivTaUWMe0+leCvAdwD89gzC\nTKbm3eDUg/6Va3X6ANh3wsfIOe4RXXxcbcFDk9R4zO2M5gfLSjYm5Q87EBZ2hrdj\nM2gLI7dt6thx0J8lR8xRHBEMrVBdgwp0g1gQzo5dAV88/kpkZVps8Q==\n-----END RSA PRIVATE KEY-----";
//            encryptedKey = "7T762KRctPvEgTnI+o9g5w/Qwts23LEF1KN04k8tPTIflN6eCdxE6+FvrHGvFhvNzpWoN23KxGquSsT3cA+7cJUJ6qspvfCbb9BkTxrqrO0TnMSNmPBM5WpUJKSiI1IFtCmzeIowGHNd1TNgiiJqAyPSFEhjdFHOSK//yrmPBLqj6U1KxuVucz4wuSo6dWrWmPL0Tq1vwcqFaVkMYujeqLlxu7tvZKgnPft3cioBSx6nJ2p00NDvP2OSXdyYTyWWOkbL/iUWnLyFd6nzNNFO+a4zI2DegfXXzIST41wmlxIVZXRy68xSjcDOVhz0xZsEe1ICyOQC2EgePvdeFGJKuRZ/sOiBTwrzOlTAIFFynVNXk/XVSnk07q9QEfOeTe4zxfF+EFTlEXOtusS0aPZriqQSI12tHOH6zre0j8gSisgJGpVqp3qvONAT37EoZVqt4Vt38WC9H8i/7/oUB6zdeGFSGbIoCt0hzt+AxDCUHQVdowE27x/6P2RzgjQ6WGQaoWxYGEmPzAY5EruzmGgpL/q4IDlh/64iW7K6P0I2ykXGPRE7Hmdyz1n82PDQakbNh4KaKhELDXSXik+VRClIE9fcdEa9tJulD2NgIKhpfwxEpAksuRkwrNFZpXzGUE36TCvvLL0yf4gV4AnVV84Isp5+yJSH9e3EeTGY5MnLwzkZ0+680Sa5SEFDvNPJyKdNYHQK2qgKqKwWolv/yEmKv50hW4q6E9SMGebX5YoM8J0B4chgn7A97edd+9DKQqkuNJQJ8OoRt5OK1P3OxGJLbJHmhgpIvX2OmqBPymX1UznKVR68p+K38FrlkgSLLsVxmAWkBfiVdLW+04rohoT4xdlqwD7xUW6ohBojGvlX94yffdJ1gDuJQ8jAczGGMh/N+TJt5gb9x6CPUt4mk7FrkLGZt8Xr67/znNL54f9YEwPBpmFP74fsk/sTS/px1/dy6vOYFuZ/SEx5VbzSv81E7wFONLBrF1H7i9dS4FELEFOztNG74Bh6Dazf1grI0RwQ9upYnpvqa+dDzk1v4JaRNxExMjir0Vo0hdPJ3JW0pQAFIX7YS3qb9d7ex0h6QtO/GDBrJMORwcmkuf4sV0kqNj5ejEvMpKfIWmh9yvf/vXnsvFAtRl8lWKctPMLE74PU5XawFAKrczwEi0vkPELRqB24KDGnBH7l+1XWszwu/Z2VvpCyx9V7Hern7n0kzYZqVxo2ZXbbJqHtxawTA8KJRKN9jLAFM/5qjxoS/jx2T1gw4snl7FAJAF9UiHeTH3W2S8bZhXNkbjIo88CZ2jtdAtfhy1IRJ6HlQbp7TwtDlP671+EMKOOaUPCADFNqYV1DlTjMJpTLuDAALGrCAafOcT85XtU118JRSWh30+dKgbEgUueNm4pRPMWvtklKSq/vAwaqoKEobnX3rLxrOhu4dqa+fEElgAHLueW0Bj7695W+ZrBFfGPKhSxYTqfWpS5ilcnyRK2A6hR/XI0qfkqnHf27sx9Myk35Uylpi/bMOg2BIXKHgUGi+GEIm1PcyIWv/6/4GsOuP8l5CBZorxIwUIyZJOuceVNT4D3tfKN10oztuqrHB0NWX29N8MHHW8OVYgIoXHROsCcDY2h00RcbUnb4GyYtwkb2xXbTqtCFduD79IuU2z4otNbySIa59pF8IeU4ax362Halu255JwaS78amjBfp6lLKb7HNCVhjzBAxjl9/ooggQadU8CF2Z3SIVob+s+oYk0qXruLcEkSGoJZ7pBnqOBTv1guaW9aYBcOaOaxlAgUNn5QIWE/lvR1IqeZmVud7jDV9lXRH4+EuY5zoJcyjuvTrIbnCSF6+OEs9MPwWS4E60ni19wL38Qel7kJjc7LVkJGeF0J1+00jrkUf2mb7TRdT8p6qTeWx8FFU/cEKjYs96kBjXOl3SHwMs+UhfY6ju/M0hsDkD0sqXMtmDrmixz/LfoFCRLhy2XVcdBec3cE4Ma4t0d3RJDHFdTAPF6/7Uaa9KZxy+r9zzB3e02azhQHssxDYw519krpLQ/FDxYh8o4+jf70Q3LLA3NyUSMT+AXI+XGlN8ZvdR6xZKbvHRM0yaiuCj4go8bzzvQByD3w16rAKveoaHzkGwtCzwJMB2u6XEh/x5jzsXVbculZ8i7RvmGmopxee1PEJnmizyNrnFlTHFKHm6QPQZ4buNo7QQ5eS44vHbqgBcH7uPs+B3JdTczlaDIeL2vBTTQYN8rYOPPZ34Tupy1XC032Z2v5iGWrQTUwIQH1TkYQR";
            sslTerminationService.setSslTerminationRepository(sslTerminationRepository);
            sslTerminationService.setLoadBalancerRepository(loadBalancerRepository);
            sslTermination = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            sslTerminationToBeUpdated = new SslTermination();
            sslTerminationToBeUpdated.setPrivatekey(workingUserKey);
            sslTerminationToBeUpdated.setCertificate(workingUserCrt);
            sslTerminationToBeUpdated.setEnabled(true);
            sslTerminationToBeUpdated.setSecureTrafficOnly(false);
            sslTerminationToBeUpdated.setIntermediateCertificate(workingUserChain);
            sslTermination.setSecurePort(443);
            sslTermination.setPrivatekey(workingUserKey);
            sslTermination.setCertificate(workingUserCrt);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(false);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            loadBalancer.setSslTermination(sslTerminationToBeUpdated);
            loadBalancer.setId(613);
            loadBalancer.setAccountId(5806065);
            iv = loadBalancer.getAccountId() + "_" + loadBalancer.getId();

            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(loadBalancer);
            when(sslTerminationRepository.getSslTerminationByLbId(anyInt(), anyInt())).thenReturn(sslTerminationToBeUpdated);
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCrypto");

        }

        @Test
        public void shouldReturnEncryptedPrivateKey() throws Exception {
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);
            Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), workingUserKey);
            String dtest = Aes.b64decryptGCM_str(zeusSslTermination.getSslTermination().getPrivatekey(), "testCrypto", iv);
            Assert.assertEquals(dtest, workingUserKey);

        }

        @Test
        public void shouldNotThrowErrorWhenPrivateKeyIsEncryptedFromDB() throws Exception {
            String privateKey = Aes.b64encryptGCM(workingUserKey.getBytes(), "testCrypto", iv);
            sslTerminationToBeUpdated.setPrivatekey(privateKey);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);
            Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), workingUserKey);
            String dtest = Aes.b64decryptGCM_str(zeusSslTermination.getSslTermination().getPrivatekey(), "testCrypto", iv);
            Assert.assertEquals(dtest, workingUserKey);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowErrorWhenCrpytoKeyIsNotFoud() throws BadRequestException, ImmutableEntityException, UnprocessableEntityException, EntityNotFoundException {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn(null);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);

        }

    }

}

