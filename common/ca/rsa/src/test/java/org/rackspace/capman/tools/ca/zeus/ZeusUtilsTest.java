/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.capman.tools.ca.zeus;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.StringUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;
import org.rackspace.capman.tools.ca.exceptions.X509PathBuildException;
import org.rackspace.capman.tools.ca.primitives.PemBlock;
import org.rackspace.capman.tools.ca.zeus.ZeusUtils;
import org.rackspace.capman.tools.ca.zeus.ZeusUtils;
import org.rackspace.capman.tools.ca.zeus.primitives.ErrorEntry;
import org.rackspace.capman.tools.ca.zeus.primitives.ZeusCrtFile;
import org.rackspace.capman.tools.util.StaticHelpers;
import org.rackspace.capman.tools.util.X509BuiltPath;
import org.rackspace.capman.tools.util.X509ChainEntry;
import org.rackspace.capman.tools.util.X509PathBuilder;
import org.rackspace.capman.tools.util.fileio.RsaFileUtils;

public class ZeusUtilsTest {

    private static KeyPair userKey;
    private static X509CertificateObject userCrt;
    private static Set<X509CertificateObject> imdCrts;
    private static X509CertificateObject rootCA;
    private static int keySize = 512; // Keeping the key small for testing
    private static List<X509ChainEntry> chainEntries;
    // These are for testing pre defined keys and certs
    private static final String workingRootCa;
    private static final String workingUserKey;
    private static final String workingUserCrt;
    private static final String workingUserChain;

    static {
        workingRootCa = "-----BEGIN CERTIFICATE-----\n"
                + "MIICoDCCAgmgAwIBAgIBATANBgkqhkiG9w0BAQUFADB8MQ8wDQYDVQQDEwZSb290\n"
                + "Q0ExIDAeBgNVBAsTF1JhY2tFeHAgQ0EgMjAxMjA1MjkxODM0MRowGAYDVQQKExFS\n"
                + "YWNrU3BhY2UgSG9zdGluZzEOMAwGA1UEBxMFVGV4YXMxDjAMBgNVBAgTBVRleGFz\n"
                + "MQswCQYDVQQGEwJVUzAeFw0xMjAxMDEwMDAwMDBaFw0zOTA1MTkwMDAwMDBaMHwx\n"
                + "DzANBgNVBAMTBlJvb3RDQTEgMB4GA1UECxMXUmFja0V4cCBDQSAyMDEyMDUyOTE4\n"
                + "MzQxGjAYBgNVBAoTEVJhY2tTcGFjZSBIb3N0aW5nMQ4wDAYDVQQHEwVUZXhhczEO\n"
                + "MAwGA1UECBMFVGV4YXMxCzAJBgNVBAYTAlVTMIGfMA0GCSqGSIb3DQEBAQUAA4GN\n"
                + "ADCBiQKBgQDKReMv4ShkwUakc69n0ROqvtcczcgbhJROOFFy+zzeB5s+YfCwPMJf\n"
                + "L5Y6Dn7XPCheSwV2Sv6MFwHFIoz3BnPWmYbUBPCyn4/1rhAY7NPOAbJKfx9oU72B\n"
                + "p5KVyWeJclBF6VPH5LsGiYfH7aAsVAxCeh9GrmxdkibXTQbUpba8LQIDAQABozIw\n"
                + "MDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBQHJYLW2WF3YOfoQGWSQuWkmzJZ\n"
                + "QjANBgkqhkiG9w0BAQUFAAOBgQACZr//V4+WGyssu7KCASm7F/KK+f9Vre0FvPvz\n"
                + "6E0YHwOripjSYm4jpL8kRw5Ad/uk2MkrP8f2UCeGi1/AdzwTVffU4XURzl6Vj1TK\n"
                + "VqzE8eL3RKDG4ALKvYZqi16cHhKG0p6cSsO5X+zCBqQ8oR6+XGsJVAqmlcVmc36E\n"
                + "g08Q9g==\n"
                + "-----END CERTIFICATE-----\n"
                + "";
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

    public ZeusUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws RsaException, NotAnX509CertificateException {
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
        String subjName = "CN=www.junit-mosso-apache2zeus-test.com";
        subjNames.add(subjName);
        chainEntries = X509PathBuilder.newChain(subjNames, keySize, notBefore, notAfter);
        int lastIdx = chainEntries.size() - 1;
        rootCA = chainEntries.get(0).getX509obj();
        userCrt = chainEntries.get(lastIdx).getX509obj();
        userKey = chainEntries.get(lastIdx).getKey();

        imdCrts = new HashSet<X509CertificateObject>();
        for (int i = 1; i < lastIdx; i++) {
            imdCrts.add(chainEntries.get(i).getX509obj());
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testZeusCertFile() throws X509PathBuildException, PemException {
        StringBuilder wtf = new StringBuilder(4096);
        StringBuilder sb = new StringBuilder(4096);
        Set<X509CertificateObject> roots = new HashSet<X509CertificateObject>();
        String rootCaStr = PemUtils.toPemString(rootCA);
        roots.add(rootCA);
        String userKeyStr = PemUtils.toPemString(userKey);
        String userCrtStr = PemUtils.toPemString(userCrt);
        List<X509CertificateObject> imdCrtsReversed = new ArrayList(imdCrts);
        Collections.reverse(imdCrtsReversed);
        for (X509CertificateObject x509obj : imdCrtsReversed) {
            sb.append(PemUtils.toPemString(x509obj));
        }
        String imdsString = sb.toString();
        ZeusUtils zu = new ZeusUtils(roots);
        ZeusCrtFile zcf = zu.buildZeusCrtFile(userKeyStr, userCrtStr, imdsString, false);
        for (ErrorEntry errors : zcf.getErrors()) {
            Throwable ex = errors.getException();
            if (ex != null) {
                wtf.append(StringUtils.getEST(ex));
            }
        }

        assertTrue(zcf.getErrors().isEmpty());
        List<PemBlock> parsedImds = PemUtils.parseMultiPem(imdsString);
        assertTrue(parsedImds.size() == 7);
        for (PemBlock block : parsedImds) {
            assertTrue(block.getDecodedObject() instanceof X509CertificateObject);
        }
    }

    @Test
    public void testLbaasValidation() throws PemException {
        Set<X509CertificateObject> roots = loadX509Set((X509CertificateObject)PemUtils.fromPemString(workingRootCa));
        Set<X509CertificateObject> blanks = loadX509Set();
        boolean expectSuccess = true;
        boolean expectErrors = false;
        boolean failOnFatal = true;
        boolean failOnAnyError = false;
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, workingUserChain, failOnFatal, expectSuccess);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, workingUserChain, failOnAnyError, expectSuccess);
        assertZCFLbaasErrors(blanks, workingUserKey, workingUserCrt, workingUserChain, failOnAnyError, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, workingUserChain, failOnFatal, expectSuccess);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, "", failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, null, failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, "", "", failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, "", null, failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, null, null, failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, "", "", "", failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, "", null, null, failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, null, null, null, failOnFatal, expectErrors);
        assertZCFLbaasErrors(roots, "", "", workingUserChain, failOnFatal, expectErrors);

        // Test if zeus failes when chain is reverse
        List<PemBlock> reverseChain = PemUtils.parseMultiPem(workingUserChain);
        Collections.reverse(reverseChain);
        String reverseChainStr = PemUtils.toMultiPemString(reverseChain);
        assertZCFLbaasErrors(roots,workingUserKey,workingUserCrt,reverseChainStr,failOnAnyError,expectErrors);
    }

    private void assertZCFLbaasErrors(Set<X509CertificateObject> roots, String key, String crt, String imd, boolean failOnlyOnFatalErrors, boolean failOnErrors) {
        ZeusUtils zu = new ZeusUtils(roots);
        ZeusCrtFile zcf = zu.buildZeusCrtFile(key, crt, imd, true);
        boolean hasError;
        String errorMsg;
        if (failOnlyOnFatalErrors) {
            hasError = zcf.hasFatalErrors();
            errorMsg = StringUtils.joinString(zcf.getFatalErrors(), ",");
        } else {
            hasError = !zcf.getErrors().isEmpty();
            errorMsg = StringUtils.joinString(zcf.getErrors(), ",");
        }

        if (failOnErrors && hasError) {
            fail(String.format("Wasn't expecting Errors: %s", errorMsg));
        } else if (!failOnErrors && !hasError) {
            fail(String.format("Was expecting errors but but no errors found %s", errorMsg));
        } else {
            // Everythings cool
            return;
        }
    }

    private Set<X509CertificateObject> loadX509Set(X509CertificateObject... x509objs) {
        Set<X509CertificateObject> x509set = new HashSet<X509CertificateObject>();
        for (int i = 0; i < x509objs.length; i++) {
            X509CertificateObject x509obj = x509objs[i];
            if (x509obj == null) {
                continue; // Cause I'm paranoid
            }
            x509set.add(x509obj);
        }
        return x509set;
    }
}
