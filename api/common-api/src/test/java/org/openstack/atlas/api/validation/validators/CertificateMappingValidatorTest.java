package org.openstack.atlas.api.validation.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@RunWith(Enclosed.class)
public class CertificateMappingValidatorTest {

    private static String workingUserKey;
    private static String workingUserCrt;
    private static String workingUserChain;

    static {
        workingUserKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIEowIBAAKCAQEAs3xsmmODvTvmokiyLL3c55gL2YemIWTq+rrHy7lf+vzG7BQL\n"
                + "3a3gyC8lG78Y/6U+jql/OTIeU6dYc+eFZs43Zz51DJVCOp6WdzLu9vWa/XNcRJPJ\n"
                + "vEy6ZKP2t8857wDKnnDMgrcWr7FUShpzhcMRO+9QthkrDoB2a9p3WffERlWsklqy\n"
                + "JoZ0IvSmKp/kklN/IL+cC5sMFGXq3xU9k9RIiE4cJ9l+ixzQdFm28VENxuDnsEOA\n"
                + "q2nhDuY24OQcZCl6iOghiGBZMB/bAkZTTtNXqhIukWVwkbr3Ar0AQbIWVIhzkSu9\n"
                + "Uhl9D537WKbYT6/pGbyXurkESZLv1TUvwj+MEwIDAQABAoIBAA1XypsPmtOOiG64\n"
                + "CMbatnlhDEB3nRoObomFKSUz+ral5APY2aj5LCS1nISbiDg5CJWCdDrERwzg++ph\n"
                + "48Dow6WhNwJgdjP1ZiomVy10xLjiwpE2i2C8M38X0VeIyD3ESUvOBuYQxlb5T9LS\n"
                + "dJQVFEroV4+x/0lss2EBNK4zhRvVUrKCITqM1cj0CPYIi1vUwSYN/5tWj2RtqlM7\n"
                + "g9EEY8IUAVGQLA6uWkad63J1Veqj+ZV4RymjidV/5VXIIrLkdB/tTgrL7BPzip20\n"
                + "B6ytVTag7PUl/JleDhNX/QkpYqs2dHSyxp9vFdrVi4arC8IxwTvu2fzMW7aSeYYu\n"
                + "TVZAIYECgYEA5n6OExMHZkLlUjVVlNB7Ff0SsA6s83m7UTUqgtyZ3WaqiPpecxro\n"
                + "K4eKtAXH/HIuTeHYuA4/kVKog0rP9tOx3llFxN6UVDvmVrdOqp5dxdS1G7lixSse\n"
                + "UuNvmouNOrKhviBVEiUe6a8LDssDwZ6H1ffF6Rwt27YBxd4DMAWvuysCgYEAx1jn\n"
                + "v55RN5tyAyPfC9lhL9KXL5iPXxFc5h9PqoD9orMYrV4qCjzLgO7mcXSINRAUUrB5\n"
                + "QLyM6fQO5Hhdr0ihRwr82KodNU1ce2ZcrvUiqL7DCG+bKzl+Z4RKb1HCtmYmnv72\n"
                + "BW/vyexgJYBCssqCRbKQOSWRBEV5nl8InmTW3rkCgYBYyXU2+cSG6svlT/Aog+p1\n"
                + "/Ode9DhBDapPTNiUUh/e/jZAz5jkY//9DJgsYnG318/oZlASDkMEWr/Y20+it8cz\n"
                + "bFYI0Oh4Th6bVr8x0BE1LIubI11dsA9dRIjwEkOT0c2mLFQ4yh3PTINI7oEC62tN\n"
                + "y8Tr19P+Z2zlaHD35ajcwwKBgQCqQYdlA+0vxgaBIhDsyNMVihHv56eDtYuXS2S6\n"
                + "JgL6A2ZvI35aUgQo8WFFwxZV447H9MsKfD8JzZUukpLJEwoTaBH9ZoUdsh3rkshT\n"
                + "8S+R2aMvQErRhwArnzQHkVfwepw/rVgn2qt81PJk9P+CPi03I8PD2w7ZDBFMLrRo\n"
                + "u408mQKBgHHwPKE82Q+z9j8Ika8N9dqtojGWdwVd/I62UybPY719pjXWkIIS/CgN\n"
                + "QLZzZftiOXBpFuvFBYxRwali6E6b2sOxMb80wd9Vvg3SDVF5lu+iJk7f510Yd5Mi\n"
                + "Fpajo5FXdPm2OyJfZBguG6ran0/4ySdIgBABmUCevMW7Hz7+w6B6\n"
                + "-----END RSA PRIVATE KEY-----\n"
                + "";
        workingUserCrt = "-----BEGIN CERTIFICATE-----\n"
                + "MIIDljCCAn6gAwIBAgIGATfn0cKNMA0GCSqGSIb3DQEBBQUAMBAxDjAMBgNVBAMT\n"
                + "BUltZCAzMB4XDTEyMDYxMzIxNDgzMloXDTE4MDEyMTIxNDgzMlowgZwxLTArBgNV\n"
                + "BAMTJHd3dy5qdW5pdC1tb3Nzby1hcGFjaGUyemV1cy10ZXN0LmNvbTEcMBoGA1UE\n"
                + "CxMTUGxhdGZvcm0gQ2xvdWRTaXRlczEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3Rp\n"
                + "bmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczELMAkGA1UE\n"
                + "BhMCVVMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCzfGyaY4O9O+ai\n"
                + "SLIsvdznmAvZh6YhZOr6usfLuV/6/MbsFAvdreDILyUbvxj/pT6OqX85Mh5Tp1hz\n"
                + "54VmzjdnPnUMlUI6npZ3Mu729Zr9c1xEk8m8TLpko/a3zznvAMqecMyCtxavsVRK\n"
                + "GnOFwxE771C2GSsOgHZr2ndZ98RGVaySWrImhnQi9KYqn+SSU38gv5wLmwwUZerf\n"
                + "FT2T1EiIThwn2X6LHNB0WbbxUQ3G4OewQ4CraeEO5jbg5BxkKXqI6CGIYFkwH9sC\n"
                + "RlNO01eqEi6RZXCRuvcCvQBBshZUiHORK71SGX0PnftYpthPr+kZvJe6uQRJku/V\n"
                + "NS/CP4wTAgMBAAGjaTBnMAwGA1UdEwEB/wQCMAAwOAYDVR0jBDEwL4AUTmYgy7tm\n"
                + "F1x6Ev4WK5BDfAAzp1ahFKQSMBAxDjAMBgNVBAMTBUltZCAyggECMB0GA1UdDgQW\n"
                + "BBQUt7P+M35E+K7aIXwlCTuvR4p/pTANBgkqhkiG9w0BAQUFAAOCAQEAlDI1ugPd\n"
                + "CX/47mCMc2ue2855OYkJQV+7Zyf9hkiCy33IXGvYD3AkwJjpT/lCmJjtbKUpfR8k\n"
                + "56VKqwLhu98HB+q89dNSLRY5sp4xFlbGZiuOaHzfdjGA+lWWCQlVQR1zLw+u3yD3\n"
                + "16zEHx0d4VWcQk0/XbNW5EHRkf5xHyMbIhU7cLqQi8aFu+b/mzk/T290mAH81j+O\n"
                + "5BatczTiIJetfhqgNwRazaOeB4qjqyd4DZIdLbBpxJKU874Xdw2hXD9JagPdrb9W\n"
                + "I7GTqx8+o9ZRRJ0U2jHxFWrMA2UG6yetbpz+zmwgPnfhcKbsxO0g9W4BLEFvQUh3\n"
                + "mSCtsdsdrlFRMg==\n"
                + "-----END CERTIFICATE-----\n"
                + "";
        workingUserChain = "-----BEGIN CERTIFICATE-----\n"
                + "MIIDBzCCAe+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADAQMQ4wDAYDVQQDEwVJbWQg\n"
                + "MjAeFw0xMjA2MTMyMTQ4MjhaFw0yMDA2MTEyMTQ4MjhaMBAxDjAMBgNVBAMTBUlt\n"
                + "ZCAzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnV3j/Y81GZjLNlmS\n"
                + "nPJkcKqbPPj67FrdWq24p8jNgMzbPSsTEc/cfP0U7S7L8FrpnwxVOIr/sA8PwhM3\n"
                + "Vpw2akZc8RZdP3tPq0wlCCvZeOhLv0HsO/WJhalbtke07vUXIaAO2NfoTd7YWRA/\n"
                + "MHh+mzCLD+igWitSNCLYrn53b59An/rZXAldFH4pV/HVyeC7P5ri84igC3SGGZP6\n"
                + "825dt6JUMgIdgEGjdo8pF0uOw9giy/tq2mEcCMp0KdAIovyoT6oZ3EM/a3iWQDuy\n"
                + "UhCYvOkxEoz0Fbr03pLRHOxzYp5n/2GWJ7xhJNQkuFfwJxq3fdYgTy1GKbutBEib\n"
                + "T9N7KwIDAQABo2wwajAPBgNVHRMBAf8EBTADAQH/MDgGA1UdIwQxMC+AFLQQwmFz\n"
                + "Ae0XOM0yMOtX6w+bOv/moRSkEjAQMQ4wDAYDVQQDEwVJbWQgMYIBAjAdBgNVHQ4E\n"
                + "FgQUTmYgy7tmF1x6Ev4WK5BDfAAzp1YwDQYJKoZIhvcNAQEFBQADggEBAByGtJC3\n"
                + "GU9RCfSaMBr3xs9THpGW+5917SiCyV8zW63WzCkOFHyGaR0LgeZLoxFBuGCVWWru\n"
                + "TR9JuVv+3PdnuiAm/cOHtVyripMXGzt5SjNZF21cueiZyysV2G/KROPcZtHBOMZK\n"
                + "hEwljMip5T+PuCC4koCjeM7567hw7UiN6pxriKwgSIScgFn7c2qt1HlYvGoVECyV\n"
                + "pHRDueuf6lk0NoZMgheN7wohlvq7VhxPCgNHn2P8T4QQVVea8Gh5UDv8JMe5Dq8X\n"
                + "FJjMAh3UTarCZPAlA7OPWlUGjJFXaPBsGoYIClBLS0d+iAnErjCkGQDmrcZKG7tf\n"
                + "/vGPc9ACpPasTfg=\n"
                + "-----END CERTIFICATE-----\n"
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIDeTCCAmGgAwIBAgIBAjANBgkqhkiG9w0BAQUFADAQMQ4wDAYDVQQDEwVJbWQg\n"
                + "MTAeFw0xMjA2MTMyMTQ4MjhaFw0yMDA2MTEyMTQ4MjhaMBAxDjAMBgNVBAMTBUlt\n"
                + "ZCAyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp5msST5GgFp5lVgT\n"
                + "PJuxXceIl0hIByXYZXWgOFcZXTNcbDhrkCVgy269urvUDfvoT66l7aTjxtW4G7QQ\n"
                + "OzpXttSUBvT1pNxAqIOjwSZT8FclGEoYUvOHpLXf/RSo3KwFAkoj6pahxqC6PCXx\n"
                + "X6NFXoUo1fiyeNaemB2zXIm2amOF/oBYuI3ViNTeP3uBgJPdgmntyQ7K8usg+X9w\n"
                + "yHIgKtLKrgqoGHZqW4SAse+NZ8t0JJukoQnbDCezIqFAHl5+IvdnVw/xyt9PFXvf\n"
                + "I2SqhKrG/qrH58J5ih1CJ3Trzwr5NWFyCi6wzLj4Jd7+iwVZeFEc8m7Tmg6P4gTU\n"
                + "7a/COQIDAQABo4HdMIHaMA8GA1UdEwEB/wQFMAMBAf8wgacGA1UdIwSBnzCBnIAU\n"
                + "+cRgG+phOy7OHVQkmANOowKno9qhgYCkfjB8MQ8wDQYDVQQDEwZSb290Q0ExIDAe\n"
                + "BgNVBAsTF1JhY2tFeHAgQ0EgMjAxMjA1MjkxODM0MRowGAYDVQQKExFSYWNrU3Bh\n"
                + "Y2UgSG9zdGluZzEOMAwGA1UEBxMFVGV4YXMxDjAMBgNVBAgTBVRleGFzMQswCQYD\n"
                + "VQQGEwJVU4IBAjAdBgNVHQ4EFgQUtBDCYXMB7Rc4zTIw61frD5s6/+YwDQYJKoZI\n"
                + "hvcNAQEFBQADggEBAGzZBGsQQzYOE5IzQhw5NMYcSO9nRNBKuRlU4ScKcwqKTsAC\n"
                + "d7THrP859A2JfqK2FWCOj95tlfzodiX5QTbDPOVxxUXCcoYwCVP83nY4yWGX21lX\n"
                + "w27pIV1jgOQCPv4NCvCJk19DdzJ+R0qE5e6Lgwl/ydJG6gvuSSvwe6xS9d7sdJo+\n"
                + "T6Jjh74+0m6Pk+ykFxK/41/qeEc/bh4Z/Rh3h8cjix4BHTXGLoEk2XkHFqfOCBsF\n"
                + "66jik/mAUgeItAURlZ//7MunoO1qk69ApwLrYXoPS5f9pvi0TKYs6v9MrJhvhzSI\n"
                + "Sr21/0AQH6y5ucamwNSysY/UikZQGjGudlHb32M=\n"
                + "-----END CERTIFICATE-----\n"
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIDZDCCAs2gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB8MQ8wDQYDVQQDEwZSb290\n"
                + "Q0ExIDAeBgNVBAsTF1JhY2tFeHAgQ0EgMjAxMjA1MjkxODM0MRowGAYDVQQKExFS\n"
                + "YWNrU3BhY2UgSG9zdGluZzEOMAwGA1UEBxMFVGV4YXMxDjAMBgNVBAgTBVRleGFz\n"
                + "MQswCQYDVQQGEwJVUzAeFw0xMjA2MTMyMTQ4MjhaFw0yMDA2MTEyMTQ4MjhaMBAx\n"
                + "DjAMBgNVBAMTBUltZCAxMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n"
                + "rUgexjLCg6UTEKZwlaxhL+Qu34DUhpdjrjf1KoYqKpNbkkdiE+pf5BCPWa/TcxzR\n"
                + "RveGQ5E6k4sdKenT+CdlcAQckInfSn6+u4Vd6mtqYRjzPjLEzTA5ri3mbV35lm73\n"
                + "JwuAvclDyC2aeIahtSNb97W+IbPHupm7Q991DdAHQFTLB3yWLfITz0F9IbD9deZa\n"
                + "he+MQy3ENaSwMGhqro5jej5n625eqQIEKFBl8IQ/U5gJwJ2W2FbUD5j8wVweVkqO\n"
                + "oK6op4Qt/5tJ3mMhHr5r12juLoWh68z6jAD8v9IlwAdgcHttQUOHd0h1XWEQbm7S\n"
                + "fMpxOH07IKZ9g3CN/2zzNQIDAQABo4HdMIHaMA8GA1UdEwEB/wQFMAMBAf8wgacG\n"
                + "A1UdIwSBnzCBnIAUByWC1tlhd2Dn6EBlkkLlpJsyWUKhgYCkfjB8MQ8wDQYDVQQD\n"
                + "EwZSb290Q0ExIDAeBgNVBAsTF1JhY2tFeHAgQ0EgMjAxMjA1MjkxODM0MRowGAYD\n"
                + "VQQKExFSYWNrU3BhY2UgSG9zdGluZzEOMAwGA1UEBxMFVGV4YXMxDjAMBgNVBAgT\n"
                + "BVRleGFzMQswCQYDVQQGEwJVU4IBATAdBgNVHQ4EFgQU+cRgG+phOy7OHVQkmANO\n"
                + "owKno9owDQYJKoZIhvcNAQEFBQADgYEAsCarpNoZN8MkaDw1gTr+1Kd1OI3fOQuG\n"
                + "hRqpQgsDUiH9vzbIEim2gtc31SLgl1cqUAnLvMNgk/CZ5nQnM6xIC9Z3DN9vezyF\n"
                + "LfqinGxwIFSwUSOKmvWhcVS2vUfL2fKmkuO794ZIwWzLUIoStRkxSjnn6CQCrFbv\n"
                + "PAearnmXp1M=\n"
                + "-----END CERTIFICATE-----\n"
                + "";
    }


    public static class WhenValidatingPost {
        private CertificateMapping certificateMapping;
        private CertificateMappingValidator validator;

        @Before
        public void standUp() {
            validator = new CertificateMappingValidator();

            certificateMapping = new CertificateMapping();
            certificateMapping.setCertificate(workingUserCrt);
            certificateMapping.setPrivateKey(workingUserKey);
            certificateMapping.setIntermediateCertificate(workingUserChain);
            certificateMapping.setHostName("somehostname.com");
        }

        @Test
        public void shouldAcceptValidCertificateMapping() {
            assertTrue(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldAcceptWhenMissingIntermediateCertificate() {
            certificateMapping.setIntermediateCertificate(null);
            assertTrue(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingPrivateKey() {
            certificateMapping.setPrivateKey(null);
            assertFalse(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingCertificate() {
            certificateMapping.setCertificate(null);
            assertFalse(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingHostName() {
            certificateMapping.setHostName(null);
            assertFalse(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingAllAttributes() {
            certificateMapping.setPrivateKey(null);
            certificateMapping.setCertificate(null);
            certificateMapping.setIntermediateCertificate(null);
            certificateMapping.setHostName(null);
            assertFalse(validator.validate(certificateMapping, POST).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdSupplied() {
            certificateMapping.setId(1);
            assertFalse(validator.validate(certificateMapping, POST).passedValidation());
        }
    }

    public static class WhenValidatingPut {
        private CertificateMapping certificateMapping;
        private CertificateMappingValidator validator;

        @Before
        public void standUp() {
            validator = new CertificateMappingValidator();

            certificateMapping = new CertificateMapping();
            certificateMapping.setCertificate(workingUserCrt);
            certificateMapping.setPrivateKey(workingUserKey);
            certificateMapping.setIntermediateCertificate(workingUserChain);
            certificateMapping.setHostName("somehostname.com");
        }

        @Test
        public void shouldAcceptValidCertificateMapping() {
            assertTrue(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenMissingIntermediateCertificate() {
            certificateMapping.setIntermediateCertificate(null);
            assertTrue(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenMissingPrivateKey() {
            certificateMapping.setPrivateKey(null);
            assertTrue(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenMissingCertificate() {
            certificateMapping.setCertificate(null);
            assertTrue(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldAcceptWhenMissingHostName() {
            certificateMapping.setHostName(null);
            assertTrue(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenMissingAllAttributes() {
            certificateMapping.setPrivateKey(null);
            certificateMapping.setCertificate(null);
            certificateMapping.setIntermediateCertificate(null);
            certificateMapping.setHostName(null);
            assertFalse(validator.validate(certificateMapping, PUT).passedValidation());
        }

        @Test
        public void shouldRejectWhenIdSupplied() {
            certificateMapping.setId(1);
            assertFalse(validator.validate(certificateMapping, PUT).passedValidation());
        }
    }
}
