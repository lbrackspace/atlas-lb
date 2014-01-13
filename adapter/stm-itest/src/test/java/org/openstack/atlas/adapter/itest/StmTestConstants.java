package org.openstack.atlas.adapter.itest;

public class StmTestConstants {
    public static final int LB_PORT = 80;
    public static final int LB_SECURE_PORT = 443;
    public static final int NODE_1_PORT = 80;
    public static final int NODE_2_PORT = 81;

    public static final Integer TEST_ACCOUNT_ID = 999998;
    public static final Integer TEST_LOADBALANCER_ID = 999998;

    public static final Integer TEST_VIP_ID = 1000041;
    public static final Integer TEST_IPV6_VIP_ID = 1000061;
    public static final Integer ADDITIONAL_VIP_ID = 88887;
    public static final Integer ADDITIONAL_IPV6_VIP_ID = 88885;

    // Concerns Connection Throttle
    public static final int MAX_CONECT_RATE = 100;
    public static final int MAX_CONNECTIONS = 30;
    public static final int MIN_CONNECTIONS = 4;
    public static final int RATE_INTERVAL = 60;

    // Concerns Error Page
    public static final String ERROR_PAGE_CONTENT = "Test Error Page";

    public static final String SSL_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIICWDCCAcGgAwIBAgIJAMihvqpCQwKsMA0GCSqGSIb3DQEBBQUAMEUxCzAJBgNV\n" +
            "BAYTAlVTMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX\n" +
            "aWRnaXRzIFB0eSBMdGQwHhcNMTQwMTEzMTY0MjE4WhcNMTQwMjEyMTY0MjE4WjBF\n" +
            "MQswCQYDVQQGEwJVUzETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50\n" +
            "ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n" +
            "gQCqRzqCE1JDySlIUbMm9GJmLOvlvt9hWmh5K8Eq52J8ntZsjSo16MRd9LdcfiUb\n" +
            "0j92kle3uHfce5PzJ5817WBz8S/+9Lq70c0cRfq1k72ngZMJ0mUHbtw2/uq34lzM\n" +
            "NUJFwx2prpvellXvfkrwNE/sDYWD6NLWJXL/csBpG3AOCQIDAQABo1AwTjAdBgNV\n" +
            "HQ4EFgQUukMx2jgTOsY2aA0EOCCY6056gfYwHwYDVR0jBBgwFoAUukMx2jgTOsY2\n" +
            "aA0EOCCY6056gfYwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOBgQAmBhQi\n" +
            "dsUL73sbPi+u/sPwooP7O8tiVYlYsW6U/RserhP2W4q8kVpLbLAVLBQZsCrC0rVN\n" +
            "VWCsAhQsQUqUt/2coUaHJcLQMQOEzc5GHqSqo7G4zsBrRAxkD3jNZHfI4chRTvUi\n" +
            "D6l5MF11fWESvJezMCViRQt4eIXNTKD5yPTc2A==\n" +
            "-----END CERTIFICATE-----";

    public static final String SSL_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKpHOoITUkPJKUhR\n" +
            "syb0YmYs6+W+32FaaHkrwSrnYnye1myNKjXoxF30t1x+JRvSP3aSV7e4d9x7k/Mn\n" +
            "nzXtYHPxL/70urvRzRxF+rWTvaeBkwnSZQdu3Db+6rfiXMw1QkXDHamum96WVe9+\n" +
            "SvA0T+wNhYPo0tYlcv9ywGkbcA4JAgMBAAECgYAeasjl0YPBfh60v3tsOv2U2PNu\n" +
            "v/DxWBAPjSRdgKXUMx+ElpbyuFjTm9JhYFtKo/0YNj4bIIWwyhZ8uhgzruqqoKXF\n" +
            "6fXlihyYaJK6kOSlomv0St98FpAbGJdbnLxq/RdAkT0Bhay1SEsk4IdDx9vI1bew\n" +
            "LeVVvLz0jKtQ01ovAQJBAN/g3R4OYDGr+OinTiTvaisRVoAJEHGx/+8SyNHCGAep\n" +
            "eph+mg7elD3Ow1z0aSDLwUv6o5eV5FLB2plzFPSgICECQQDCtZybcGMPaLLEL7aw\n" +
            "I+0EmdOJNQq76KSDnuZLRgSYQpQYELt2wVCfJmJYqLhL9xYPAbof1CSTSJy2JHhI\n" +
            "mNDpAkBoMln3cS1oMOgKMEP4gM1i+hDOSmmy4OuDM7Tvm2xftItwigvdu8427hKT\n" +
            "ItYDA5IVcLPPC15W/g5luOAZ8qTBAkBizxscXppGtZESskv5cyHS1eVdmcdKuLLU\n" +
            "AI1J4cxvIEpbNBKOH2g0e5wo9eYg1tEg6HV1tYiiHZU5caSA4twRAkAGofSSv0Vz\n" +
            "gmasCOgV+x3nG37Vi+CrBYheMG9gLUXbG6wqZGkQnnmPv+a59M544uRAsm6MPzPK\n" +
            "yyQhSX2kIkHu\n" +
            "-----END PRIVATE KEY-----";
}