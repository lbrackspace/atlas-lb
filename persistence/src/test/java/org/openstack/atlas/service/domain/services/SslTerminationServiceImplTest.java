package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
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
import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;
import org.openstack.atlas.service.domain.services.impl.SslCipherProfileServiceImpl;
import org.openstack.atlas.service.domain.services.impl.SslTerminationServiceImpl;
import org.openstack.atlas.util.b64aes.Aes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
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

        LoadBalancer loadBalancer;
        org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination sslTermination;
        SslTermination sslTerminationToBeUpdated;
        String privateKey;
        String encryptedKey;
        String iv;



        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadBalancer = new LoadBalancer();
            privateKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEAwIudSMpRZx7TS0/AtDVX3DgXwLD9g+XrNaoazlhwhpYALgzJ\nLAbAnOxT6OT0gTpkPus/B7QhW6y6Auf2cdBeW31XoIwPsSoyNhxgErGBxzNARRB9\nlI1HCa1ojFrcULluj4W6rpaOycI5soDBJiJHin/hbZBPZq6vhPCuNP7Ya48Zd/2X\nCQ9ft3XKfmbs1SdrdROIhigse/SGRbMrCorn/vhNIuohr7yOlHG3GcVcUI9k6ZSZ\nBbqF+ZA4ApSF/Q6/cumieEgofhkYbx5fg02s9Jwr4IWnIR2bSHs7UQ6sVgKYzjs7\nPd3Unpa74jFw6/H6shABoO2CIYLotGmQbFgnpwIDAQABAoIBAQCBCQ+PCIclJHNV\ntUzfeCA5ZR4F9JbxHdRTUnxEbOB8UWotckQfTScoAvj4yvdQ42DrCZxj/UOdvFOs\nPufZvlp91bIz1alugWjE+p8n5+2hIaegoTyHoWZKBfxak0myj5KYfHZvKlbmv1ML\nXV4TwEVRfAIG+v87QTY/UUxuF5vR+BpKIbgUJLfPUFFvJUdl84qsJ44pToxaYUd/\nh5YAGC00U4ay1KVSAUnTkkPNZ0lPG/rWU6w6WcTvNRLMd8DzFLTKLOgQfHhbExAF\n+sXPWjWSzbBRP1O7fHqq96QQh4VFiY/7w9W+sDKQyV6Ul17OSXs6aZ4f+lq4rJTI\n1FG96YiBAoGBAO1tiH0h1oWDBYfJB3KJJ6CQQsDGwtHo/DEgznFVP4XwEVbZ98Ha\nBfBCn3sAybbaikyCV1Hwj7kfHMZPDHbrcUSFX7quu/2zPK+wO3lZKXSyu4YsguSa\nRedInN33PpdnlPhLyQdWSuD5sVHJDF6xn22vlyxeILH3ooLg2WOFMPmVAoGBAM+b\nUG/a7iyfpAQKYyuFAsXz6SeFaDY+ZYeX45L112H8Pu+Ie/qzon+bzLB9FIH8GP6+\nQpQgmm/p37U2gD1zChUv7iW6OfQBKk9rWvMpfRF6d7YHquElejhizfTZ+ntBV/VY\ndOYEczxhrdW7keLpatYaaWUy/VboRZmlz/9JGqVLAoGAHfqNmFc0cgk4IowEj7a3\ntTNh6ltub/i+FynwRykfazcDyXaeLPDtfQe8gVh5H8h6W+y9P9BjJVnDVVrX1RAn\nbiJ1EupLPF5sVDapW8ohTOXgfbGTGXBNUUW+4Nv+IDno+mz/RhjkPYHpnM0I7c/5\ntGzOZsC/2hjNgT8I0+MWav0CgYEAuULdJeQVlKalI6HtW2Gn1uRRVJ49H+LQkY6e\nW3+cw2jo9LI0CMWSphNvNrN3wIMp/vHj0fHCP0pSApDvIWbuQXfzKaGko7UCf7rK\nf6GvZRCHkV4IREBAb97j8bMvThxClMNqFfU0rFZyXP+0MOyhFQyertswrgQ6T+Fi\n2mnvKD8CgYAmJHP3NTDRMoMRyAzonJ6nEaGUbAgNmivTaUWMe0+leCvAdwD89gzC\nTKbm3eDUg/6Va3X6ANh3wsfIOe4RXXxcbcFDk9R4zO2M5gfLSjYm5Q87EBZ2hrdj\nM2gLI7dt6thx0J8lR8xRHBEMrVBdgwp0g1gQzo5dAV88/kpkZVps8Q==\n-----END RSA PRIVATE KEY-----";
            encryptedKey = "7T762KRctPvEgTnI+o9g5w/Qwts23LEF1KN04k8tPTIflN6eCdxE6+FvrHGvFhvNzpWoN23KxGquSsT3cA+7cJUJ6qspvfCbb9BkTxrqrO0TnMSNmPBM5WpUJKSiI1IFtCmzeIowGHNd1TNgiiJqAyPSFEhjdFHOSK//yrmPBLqj6U1KxuVucz4wuSo6dWrWmPL0Tq1vwcqFaVkMYujeqLlxu7tvZKgnPft3cioBSx6nJ2p00NDvP2OSXdyYTyWWOkbL/iUWnLyFd6nzNNFO+a4zI2DegfXXzIST41wmlxIVZXRy68xSjcDOVhz0xZsEe1ICyOQC2EgePvdeFGJKuRZ/sOiBTwrzOlTAIFFynVNXk/XVSnk07q9QEfOeTe4zxfF+EFTlEXOtusS0aPZriqQSI12tHOH6zre0j8gSisgJGpVqp3qvONAT37EoZVqt4Vt38WC9H8i/7/oUB6zdeGFSGbIoCt0hzt+AxDCUHQVdowE27x/6P2RzgjQ6WGQaoWxYGEmPzAY5EruzmGgpL/q4IDlh/64iW7K6P0I2ykXGPRE7Hmdyz1n82PDQakbNh4KaKhELDXSXik+VRClIE9fcdEa9tJulD2NgIKhpfwxEpAksuRkwrNFZpXzGUE36TCvvLL0yf4gV4AnVV84Isp5+yJSH9e3EeTGY5MnLwzkZ0+680Sa5SEFDvNPJyKdNYHQK2qgKqKwWolv/yEmKv50hW4q6E9SMGebX5YoM8J0B4chgn7A97edd+9DKQqkuNJQJ8OoRt5OK1P3OxGJLbJHmhgpIvX2OmqBPymX1UznKVR68p+K38FrlkgSLLsVxmAWkBfiVdLW+04rohoT4xdlqwD7xUW6ohBojGvlX94yffdJ1gDuJQ8jAczGGMh/N+TJt5gb9x6CPUt4mk7FrkLGZt8Xr67/znNL54f9YEwPBpmFP74fsk/sTS/px1/dy6vOYFuZ/SEx5VbzSv81E7wFONLBrF1H7i9dS4FELEFOztNG74Bh6Dazf1grI0RwQ9upYnpvqa+dDzk1v4JaRNxExMjir0Vo0hdPJ3JW0pQAFIX7YS3qb9d7ex0h6QtO/GDBrJMORwcmkuf4sV0kqNj5ejEvMpKfIWmh9yvf/vXnsvFAtRl8lWKctPMLE74PU5XawFAKrczwEi0vkPELRqB24KDGnBH7l+1XWszwu/Z2VvpCyx9V7Hern7n0kzYZqVxo2ZXbbJqHtxawTA8KJRKN9jLAFM/5qjxoS/jx2T1gw4snl7FAJAF9UiHeTH3W2S8bZhXNkbjIo88CZ2jtdAtfhy1IRJ6HlQbp7TwtDlP671+EMKOOaUPCADFNqYV1DlTjMJpTLuDAALGrCAafOcT85XtU118JRSWh30+dKgbEgUueNm4pRPMWvtklKSq/vAwaqoKEobnX3rLxrOhu4dqa+fEElgAHLueW0Bj7695W+ZrBFfGPKhSxYTqfWpS5ilcnyRK2A6hR/XI0qfkqnHf27sx9Myk35Uylpi/bMOg2BIXKHgUGi+GEIm1PcyIWv/6/4GsOuP8l5CBZorxIwUIyZJOuceVNT4D3tfKN10oztuqrHB0NWX29N8MHHW8OVYgIoXHROsCcDY2h00RcbUnb4GyYtwkb2xXbTqtCFduD79IuU2z4otNbySIa59pF8IeU4ax362Halu255JwaS78amjBfp6lLKb7HNCVhjzBAxjl9/ooggQadU8CF2Z3SIVob+s+oYk0qXruLcEkSGoJZ7pBnqOBTv1guaW9aYBcOaOaxlAgUNn5QIWE/lvR1IqeZmVud7jDV9lXRH4+EuY5zoJcyjuvTrIbnCSF6+OEs9MPwWS4E60ni19wL38Qel7kJjc7LVkJGeF0J1+00jrkUf2mb7TRdT8p6qTeWx8FFU/cEKjYs96kBjXOl3SHwMs+UhfY6ju/M0hsDkD0sqXMtmDrmixz/LfoFCRLhy2XVcdBec3cE4Ma4t0d3RJDHFdTAPF6/7Uaa9KZxy+r9zzB3e02azhQHssxDYw519krpLQ/FDxYh8o4+jf70Q3LLA3NyUSMT+AXI+XGlN8ZvdR6xZKbvHRM0yaiuCj4go8bzzvQByD3w16rAKveoaHzkGwtCzwJMB2u6XEh/x5jzsXVbculZ8i7RvmGmopxee1PEJnmizyNrnFlTHFKHm6QPQZ4buNo7QQ5eS44vHbqgBcH7uPs+B3JdTczlaDIeL2vBTTQYN8rYOPPZ34Tupy1XC032Z2v5iGWrQTUwIQH1TkYQR";
            sslTerminationService.setSslTerminationRepository(sslTerminationRepository);
            sslTerminationService.setLoadBalancerRepository(loadBalancerRepository);
            sslTermination = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            sslTerminationToBeUpdated = new SslTermination();
            sslTerminationToBeUpdated.setPrivatekey(encryptedKey);
            sslTerminationToBeUpdated.setCertificate("-----BEGIN CERTIFICATE-----\nMIIEXTCCA0WgAwIBAgIGATTEAjK3MA0GCSqGSIb3DQEBBQUAMIGDMRkwFwYDVQQD\nExBUZXN0IENBIFNUdWIgS2V5MRcwFQYDVQQLEw5QbGF0Zm9ybSBMYmFhczEaMBgG\nA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4w\nDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwHhcNMTIwMTA5MTk0NjQ1WhcNMTQw\nMTA4MTk0NjQ1WjCBgjELMAkGA1UEBhMCVVMxDjAMBgNVBAgTBVRleGFzMRQwEgYD\nVQQHEwtTYW4gQW50b25pbzEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFzAV\nBgNVBAsTDlBsYXRmb3JtIExiYWFzMRgwFgYDVQQDEw9UZXN0IENsaWVudCBLZXkw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDAi51IylFnHtNLT8C0NVfc\nOBfAsP2D5es1qhrOWHCGlgAuDMksBsCc7FPo5PSBOmQ+6z8HtCFbrLoC5/Zx0F5b\nfVegjA+xKjI2HGASsYHHM0BFEH2UjUcJrWiMWtxQuW6Phbqulo7JwjmygMEmIkeK\nf+FtkE9mrq+E8K40/thrjxl3/ZcJD1+3dcp+ZuzVJ2t1E4iGKCx79IZFsysKiuf+\n+E0i6iGvvI6UcbcZxVxQj2TplJkFuoX5kDgClIX9Dr9y6aJ4SCh+GRhvHl+DTaz0\nnCvghachHZtIeztRDqxWApjOOzs93dSelrviMXDr8fqyEAGg7YIhgui0aZBsWCen\nAgMBAAGjgdUwgdIwgbAGA1UdIwSBqDCBpYAUNpx1Pc6cGA7KqEwHMmHBTZMA7lSh\ngYmkgYYwgYMxGTAXBgNVBAMTEFRlc3QgQ0EgU1R1YiBLZXkxFzAVBgNVBAsTDlBs\nYXRmb3JtIExiYWFzMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UE\nBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVU4IBATAd\nBgNVHQ4EFgQULueOfsjZZOHwJHZwBy6u0swnpccwDQYJKoZIhvcNAQEFBQADggEB\nAFNuqSVUaotUJoWDv4z7Kbi6JFpTjDht5ORw4BdVYlRD4h9DACAFzPrPV2ym/Osp\nhNMdZq6msZku7MdOSQVhdeGWrSNk3M8O9Hg7cVzPNXOF3iNoo3irQ5tURut44xs4\nWw5YWQqS9WyUY5snD8tm7Y1rQTPfhg+678xIq/zWCv/u+FSnfVv1nlhLVQkEeG/Y\ngh1uMaTIpUKTGEjIAGtpGP7wwIcXptR/HyfzhTUSTaWc1Ef7zoKT9LL5z3IV1hC2\njVWz+RwYs98LjMuksJFoHqRfWyYhCIym0jb6GTwaEmpxAjc+d7OLNQdnoEGoUYGP\nYjtfkRYg265ESMA+Kww4Xy8=\n-----END CERTIFICATE-----\n");
            sslTerminationToBeUpdated.setEnabled(true);
            sslTerminationToBeUpdated.setSecureTrafficOnly(false);
            sslTerminationToBeUpdated.setIntermediateCertificate("-----BEGIN CERTIFICATE-----\nMIIDtTCCAp2gAwIBAgIBATANBgkqhkiG9w0BAQUFADCBgzEZMBcGA1UEAxMQVGVz\ndCBDQSBTVHViIEtleTEXMBUGA1UECxMOUGxhdGZvcm0gTGJhYXMxGjAYBgNVBAoT\nEVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UE\nCBMFVGV4YXMxCzAJBgNVBAYTAlVTMB4XDTEyMDEwOTE5NDU0OVoXDTE0MDEwODE5\nNDU0OVowgYMxGTAXBgNVBAMTEFRlc3QgQ0EgU1R1YiBLZXkxFzAVBgNVBAsTDlBs\nYXRmb3JtIExiYWFzMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UE\nBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVUzCCASIw\nDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNh55lwTVwQvNoEZjq1zGdYz9jA\nXXdjizn8AJhjHLOAallfPtvCfTEgKanhdoyz5FnhQE8HbDAop/KNS1lN2UMvdl5f\nZNLTSjJrNtedqxQwxN/i3bpyBxNVejUH2NjV1mmyj+5CJYwCzWalvI/gLPq/A3as\nO2EQqtf3U8unRgn0zXLRdYxV9MrUzNAmdipPNvNrsVdrCgA42rgF/8KsyRVQfJCX\nfN7PGCfrsC3YaUvhymraWxNnXIzMYTNa9wEeBZLUw8SlEtpa1Zsvui+TPXu3USNZ\nVnWH8Lb6ENlnoX0VBwo62fjOG3JzhNKoJawi3bRqyDdINOvafr7iPrrs/T8CAwEA\nAaMyMDAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUNpx1Pc6cGA7KqEwHMmHB\nTZMA7lQwDQYJKoZIhvcNAQEFBQADggEBAMoRgH3iTG3t317viLKoY+lNMHUgHuR7\nb3mn9MidJKyYVewe6hCDIN6WY4fUojmMW9wFJWJIo0hRMNHL3n3tq8HP2j20Mxy8\nacPdfGZJa+jiBw72CrIGdobKaFduIlIEDBA1pNdZIJ+EulrtqrMesnIt92WaypIS\n8JycbIgDMCiyC0ENHEk8UWlC6429c7OZAsplMTbHME/1R4btxjkdfrYZJjdJ2yL2\n8cjZDUDMCPTdW/ycP07Gkq30RB5tACB5aZdaCn2YaKC8FsEdhff4X7xEOfOEHWEq\nSRxADDj8Lx1MT6QpR07hCiDyHfTCtbqzI0iGjX63Oh7xXSa0f+JVTa8=\n-----END CERTIFICATE-----\n");
            sslTermination.setSecurePort(443);
            sslTermination.setPrivatekey(privateKey);
            sslTermination.setCertificate("-----BEGIN CERTIFICATE-----\nMIIEXTCCA0WgAwIBAgIGATTEAjK3MA0GCSqGSIb3DQEBBQUAMIGDMRkwFwYDVQQD\nExBUZXN0IENBIFNUdWIgS2V5MRcwFQYDVQQLEw5QbGF0Zm9ybSBMYmFhczEaMBgG\nA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4w\nDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwHhcNMTIwMTA5MTk0NjQ1WhcNMTQw\nMTA4MTk0NjQ1WjCBgjELMAkGA1UEBhMCVVMxDjAMBgNVBAgTBVRleGFzMRQwEgYD\nVQQHEwtTYW4gQW50b25pbzEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFzAV\nBgNVBAsTDlBsYXRmb3JtIExiYWFzMRgwFgYDVQQDEw9UZXN0IENsaWVudCBLZXkw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDAi51IylFnHtNLT8C0NVfc\nOBfAsP2D5es1qhrOWHCGlgAuDMksBsCc7FPo5PSBOmQ+6z8HtCFbrLoC5/Zx0F5b\nfVegjA+xKjI2HGASsYHHM0BFEH2UjUcJrWiMWtxQuW6Phbqulo7JwjmygMEmIkeK\nf+FtkE9mrq+E8K40/thrjxl3/ZcJD1+3dcp+ZuzVJ2t1E4iGKCx79IZFsysKiuf+\n+E0i6iGvvI6UcbcZxVxQj2TplJkFuoX5kDgClIX9Dr9y6aJ4SCh+GRhvHl+DTaz0\nnCvghachHZtIeztRDqxWApjOOzs93dSelrviMXDr8fqyEAGg7YIhgui0aZBsWCen\nAgMBAAGjgdUwgdIwgbAGA1UdIwSBqDCBpYAUNpx1Pc6cGA7KqEwHMmHBTZMA7lSh\ngYmkgYYwgYMxGTAXBgNVBAMTEFRlc3QgQ0EgU1R1YiBLZXkxFzAVBgNVBAsTDlBs\nYXRmb3JtIExiYWFzMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UE\nBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVU4IBATAd\nBgNVHQ4EFgQULueOfsjZZOHwJHZwBy6u0swnpccwDQYJKoZIhvcNAQEFBQADggEB\nAFNuqSVUaotUJoWDv4z7Kbi6JFpTjDht5ORw4BdVYlRD4h9DACAFzPrPV2ym/Osp\nhNMdZq6msZku7MdOSQVhdeGWrSNk3M8O9Hg7cVzPNXOF3iNoo3irQ5tURut44xs4\nWw5YWQqS9WyUY5snD8tm7Y1rQTPfhg+678xIq/zWCv/u+FSnfVv1nlhLVQkEeG/Y\ngh1uMaTIpUKTGEjIAGtpGP7wwIcXptR/HyfzhTUSTaWc1Ef7zoKT9LL5z3IV1hC2\njVWz+RwYs98LjMuksJFoHqRfWyYhCIym0jb6GTwaEmpxAjc+d7OLNQdnoEGoUYGP\nYjtfkRYg265ESMA+Kww4Xy8=\n-----END CERTIFICATE-----\n");
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
           Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), privateKey);
           String dtest = Aes.b64decryptGCM_str(zeusSslTermination.getSslTermination().getPrivatekey(), "testCrypto", iv);
           Assert.assertEquals(dtest, privateKey);

        }

        @Test
        public void shouldNotThrowErrorWhenPrivateKeyIsNotEncrptedFromDB() throws Exception {
            sslTerminationToBeUpdated.setPrivatekey(privateKey);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);
;           Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), privateKey);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowErrorWhenCrpytoKeyIsNotFoud() throws BadRequestException, ImmutableEntityException, UnprocessableEntityException, EntityNotFoundException {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn(null);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);

        }

    }

}
