package org.openstack.atlas.adapter.itest;

public class VTMTestConstants {

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
    public static final String CIPHER_LIST =  "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384, SSL_ECDHE_RSA_WITH_AES_128_GCM_SHA256, " +
            "SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA384, SSL_ECDHE_RSA_WITH_AES_256_CBC_SHA, " +
            "SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA256, SSL_ECDHE_RSA_WITH_AES_128_CBC_SHA, " +
            "SSL_RSA_WITH_AES_256_GCM_SHA384, SSL_RSA_WITH_AES_256_CBC_SHA256, " +
            "SSL_RSA_WITH_AES_256_CBC_SHA, SSL_RSA_WITH_AES_128_GCM_SHA256, " +
            "SSL_RSA_WITH_AES_128_CBC_SHA256, SSL_RSA_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    public static final String SSL_KEY = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEowIBAAKCAQEAgqD3/FFqkcbYkB1ucRtt0PcQQgrmviNW/8xmehxCVAZleyMZ\n"
            + "+T5j8ewz/wul7l3ozz8vBUn7+bOXbMVQI0qUgWtZDEcgHB9lKbhWBEnO/SKhIRA6\n"
            + "DgpxxQPhwr2JCWaFgmmOGCabhpQlv3NMNosErx8AtRdQRW4R/015ns5URWnMd67O\n"
            + "5pMUOr1ZMdALDEO/NXllKFWT1VubWcI8lGY25KviFMd8oUxLp3FgArQsnI4meVVK\n"
            + "tFjgxl3k7w3MdkQMdWGDIkszjbSquvOp7qBIHeTTI1x/OdIEXNtk+KshYgNKYipw\n"
            + "6svN9GMfCUMgyYXGcFbKNsICaJxSgw7vHCyLCwIDAQABAoIBAFE9uV1EaWoGaDOh\n"
            + "CknwDClVLthUHVONeRMX1XgGUT4eyEh/+SxpIBGanG/3l5ga8arLGfxJGhwfFIMC\n"
            + "aCe6v4eJ8tTO+2piIDGFQoHiQYhvXBTLGcAXwNAN7P+Hj024ZDpKWaQthrZ5NszN\n"
            + "8U1OwngDaRY3TKYRocHNNmGM4x5TmLsCoC02PQp372HmNQTi+cVIE/NqSY+kKOpt\n"
            + "JtkDyjg6HHvn/2vWFSfNUAIKQV21gI/yUsjxDIwhvAqZuUMc803/++ripn7X/4U7\n"
            + "u468ftyzX8czohKSTsCfPTwc6R+aPY57WP8Y/H4ND42qFCxvuXAB0PxUFRmybdAo\n"
            + "w/FLnwkCgYEA4mKtZiaB53IMoEZEuxdnj+y8WTmiW/gEonqA8rPKipCobyQ6n2H2\n"
            + "A0jSvLzKBfLpK0lyzEEWDnYjZIUEE1DEW3MwkWGAj5SPsnFkeaHs98cp1DJe/W4r\n"
            + "yRed/h69hdIfT0qxjhXxfjG5Z24lrzRb9Um20cAC9ZTUYV+6yUenrwUCgYEAk7eJ\n"
            + "0jgFmqTFPIeLJKy8yf3g2IiKf/M2GEdfLWLzMeb9KExanzTdHpJLm0TlP9rzIbrJ\n"
            + "K95LJZb0gDLUvtAUCrSNWSrqP99nCrUhHlvPz/uRskr6sqCq07msfDqe2Dzku8hi\n"
            + "fFNeEr3y6PzKVj0wQwyJ/neoFbDeb8fh8WXAzs8CgYAJQQ1jiplu05uuhZDTsQ47\n"
            + "tOqyHwgDCG1wEt/oi+7woR8xg+Kdl0yOL9DlhDYh7h8X9OWrcdGr/6pMtp9HBtsv\n"
            + "/dJzbu/pbqI9IiOAUV01d7+++9wkMfe6Pavosmr+6Gr9O8lsTHtVtESm9Lq3ocVM\n"
            + "jpnZkybL0SypWldxWOuC3QKBgH8dqYdy2De85VJyXeYAlDHKvMv5rB3xYLLB8ICx\n"
            + "LxDZcy9TjYSFzGb7g/6o6hLqSuHSyD1s/QPrBJqxo2xUyXpw51Y9XPn8OML5Hffv\n"
            + "EEUterKQXW6MXpT2qhz5oSn6NOf3x/CvGTxNo5SlvUU7QKXiS3nk5ItN+gz9WE0Q\n"
            + "wDv3AoGBAMjFdF7wCYNRR6uKfmqV0qrxjk5ChlHAw7BruGlC/eMoPhLrQQ5bfaX0\n"
            + "B9DoFEVoUwngljDkZ8JPeyC2EPthKQ4p9qLcxHT7SwK3PvzEenCBjSstqE2ocjJY\n"
            + "N//bkwy/AXr5nQkfFFjAfkqhL/zay/9tH6eSsw54V1E/6F/7Bpvi\n"
            + "-----END RSA PRIVATE KEY-----\n"
            + "";
    public static final String SSL_CERT = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIEAzCCAuugAwIBAgIBATANBgkqhkiG9w0BAQsFADCBqjE2MDQGA1UEAwwtd3d3\n"
            + "LnJhY2tleHAub3JnL2VtYWlsQWRkcmVzcz1yb290QHJhY2tleHAub3JnMR0wGwYD\n"
            + "VQQLExRDbG91ZCAgTG9hZEJhbGFuY2VyczEeMBwGA1UEChMVUmFja3NwYWNlIEhv\n"
            + "c3RpbmcgSU5DMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4YXMx\n"
            + "CzAJBgNVBAYTAlVTMB4XDTE0MDExNDIxMjY1M1oXDTIxMDQxNzIxMjY1M1owgaox\n"
            + "NjA0BgNVBAMMLXd3dy5yYWNrZXhwLm9yZy9lbWFpbEFkZHJlc3M9cm9vdEByYWNr\n"
            + "ZXhwLm9yZzEdMBsGA1UECxMUQ2xvdWQgIExvYWRCYWxhbmNlcnMxHjAcBgNVBAoT\n"
            + "FVJhY2tzcGFjZSBIb3N0aW5nIElOQzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAM\n"
            + "BgNVBAgTBVRleGFzMQswCQYDVQQGEwJVUzCCASIwDQYJKoZIhvcNAQEBBQADggEP\n"
            + "ADCCAQoCggEBAIKg9/xRapHG2JAdbnEbbdD3EEIK5r4jVv/MZnocQlQGZXsjGfk+\n"
            + "Y/HsM/8Lpe5d6M8/LwVJ+/mzl2zFUCNKlIFrWQxHIBwfZSm4VgRJzv0ioSEQOg4K\n"
            + "ccUD4cK9iQlmhYJpjhgmm4aUJb9zTDaLBK8fALUXUEVuEf9NeZ7OVEVpzHeuzuaT\n"
            + "FDq9WTHQCwxDvzV5ZShVk9Vbm1nCPJRmNuSr4hTHfKFMS6dxYAK0LJyOJnlVSrRY\n"
            + "4MZd5O8NzHZEDHVhgyJLM420qrrzqe6gSB3k0yNcfznSBFzbZPirIWIDSmIqcOrL\n"
            + "zfRjHwlDIMmFxnBWyjbCAmicUoMO7xwsiwsCAwEAAaMyMDAwDwYDVR0TAQH/BAUw\n"
            + "AwEB/zAdBgNVHQ4EFgQUvQn5I55Jn634ons386JCTrcx2VgwDQYJKoZIhvcNAQEL\n"
            + "BQADggEBAGhKUnFtBbHUecOnJQk8Qnli46Z8Ut4M7Q+AZQ4S6UyZGgXD9Rh30cXx\n"
            + "ky2482WROw2gWL2cR/GlADBPXDIaB80HKHOcnrAMPpCdeGamkwgQdh+csK0Lhu4b\n"
            + "FBWkHilZISZYwJ6WxCCjx8810NVDHILA8gmWCiYhTNvVhmoobOcaBg396QiRf8/q\n"
            + "CNZ4Nlg8SgtCw/bNG0LfpeBTUQo+wduSx/GWwfMdatgYWxRj94Gp7ko4UnUTqYGe\n"
            + "XGHekyCBlaj8Riz89qzCXPxKZwkuK/MUxXbA0XHDnvP0MuG6CQIEol+ddrzq9Pnl\n"
            + "H6PjnH7oMFyiMRj7ZZ/b4FwaC6uCjl4=\n"
            + "-----END CERTIFICATE-----\n"
            + "";

    public static final String CMAPPINGS_INTERMEDIATES = ""
            + "-----BEGIN CERTIFICATE-----\n" +
            "MIIDhzCCAvCgAwIBAgIGAWzZx58MMA0GCSqGSIb3DQEBCwUAMIGLMQswCQYDVQQG\n" +
            "EwJVUzELMAkGA1UECAwCVFgxFDASBgNVBAcMC1NhbiBBbnRvbmlvMRowGAYDVQQK\n" +
            "DBFSYWNrc3BhY2UgSG9zdGluZzEcMBoGA1UECwwTQ2xvdWQgTG9hZEJhbGFuY2lu\n" +
            "ZzEfMB0GA1UEAwwWTGJhYXMgRXhhbXBsZSBJbWQyIGNydDAeFw0xOTA4MjgxOTUw\n" +
            "MzNaFw0yMTA4MjcxOTUwMzNaMIGLMQswCQYDVQQGEwJVUzELMAkGA1UECAwCVFgx\n" +
            "FDASBgNVBAcMC1NhbiBBbnRvbmlvMRowGAYDVQQKDBFSYWNrc3BhY2UgSG9zdGlu\n" +
            "ZzEcMBoGA1UECwwTQ2xvdWQgTG9hZEJhbGFuY2luZzEfMB0GA1UEAwwWTGJhYXMg\n" +
            "RXhhbXBsZSBJbWQzIGNydDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA+uwn\n" +
            "uhAbB8GIR7aqxfzgD6fjIcwSMRNVV5Df03CK1oFGeSBm7oyogxDzFhdKETBATd1w\n" +
            "02t0h0BKb11MGT69tHkejNBEQh4WvUFHbcqu+2ohqjCGjlb2drmn6leSS+wkeYT8\n" +
            "2DlY2a3SD8kklWNTJpclxjZ4gKB0mVo8a0VQ9XECAwEAAaOB8zCB8DAPBgNVHRMB\n" +
            "Af8EBTADAQH/MIG9BgNVHSMEgbUwgbKAFPaP/anatJn8Y5zCzit00GGE4YdCoYGR\n" +
            "pIGOMIGLMQswCQYDVQQGEwJVUzELMAkGA1UECAwCVFgxFDASBgNVBAcMC1NhbiBB\n" +
            "bnRvbmlvMRowGAYDVQQKDBFSYWNrc3BhY2UgSG9zdGluZzEcMBoGA1UECwwTQ2xv\n" +
            "dWQgTG9hZEJhbGFuY2luZzEfMB0GA1UEAwwWTGJhYXMgRXhhbXBsZSBJbWQxIGNy\n" +
            "dIIGAWzZx57PMB0GA1UdDgQWBBTN2E73ka49l2dToy2KNf02iZzybzANBgkqhkiG\n" +
            "9w0BAQsFAAOBgQCMz+lj8DilUghmutyCDayrQ9yyvgDkDTvqkvggVi//gBrWSAdu\n" +
            "bBTaJZ40sFllXpwBfM8z+tjwtd4nfPjn/fdgLuO2CyogG9VTfZzqH+1ZxkN5Ci6y\n" +
            "6W9nkcPBhTpLVCfH+apTe+bF61CvnWjveBGBoMJ/bav2K7hDhkxYrO/X0w==\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDhTCCAu6gAwIBAgIGAWzZx57PMA0GCSqGSIb3DQEBCwUAMIGLMQswCQYDVQQG\n" +
            "EwJVUzELMAkGA1UECAwCVFgxFDASBgNVBAcMC1NhbiBBbnRvbmlvMRowGAYDVQQK\n" +
            "DBFSYWNrc3BhY2UgSG9zdGluZzEcMBoGA1UECwwTQ2xvdWQgTG9hZEJhbGFuY2lu\n" +
            "ZzEfMB0GA1UEAwwWTGJhYXMgRXhhbXBsZSBJbWQxIGNydDAeFw0xOTA4MjgxOTUw\n" +
            "MzNaFw0yMTA4MjcxOTUwMzNaMIGLMQswCQYDVQQGEwJVUzELMAkGA1UECAwCVFgx\n" +
            "FDASBgNVBAcMC1NhbiBBbnRvbmlvMRowGAYDVQQKDBFSYWNrc3BhY2UgSG9zdGlu\n" +
            "ZzEcMBoGA1UECwwTQ2xvdWQgTG9hZEJhbGFuY2luZzEfMB0GA1UEAwwWTGJhYXMg\n" +
            "RXhhbXBsZSBJbWQyIGNydDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAsngt\n" +
            "M1Ff7CJC5TZivVclxI6ZA8NI0XtTi1Yl3LhdMQ1XsTFx7TYdAb4B63HWPrinQss+\n" +
            "bOPm+n6+d78w0dRQWE1iO/2P+a87ITqZtVwBIhxVulrUuytXh+culn10U4XNo/7v\n" +
            "DRi8OKjxDziZwZpRex6hadGRmoftHObsQbUdOAkCAwEAAaOB8TCB7jAPBgNVHRMB\n" +
            "Af8EBTADAQH/MIG7BgNVHSMEgbMwgbCAFLAdaDpQqieC36VRlstZw8GFq74moYGP\n" +
            "pIGMMIGJMR0wGwYDVQQDDBRSb290Q2EgTGJhYXMgRXhhbXBsZTEcMBoGA1UECwwT\n" +
            "Q2xvdWQgTG9hZEJhbGFuY2luZzEaMBgGA1UECgwRUmFja3NwYWNlIEhvc3Rpbmcx\n" +
            "FDASBgNVBAcMC1NhbiBBbnRvbmlvMQswCQYDVQQIDAJUWDELMAkGA1UEBhMCVVOC\n" +
            "BgFs2ceenjAdBgNVHQ4EFgQU9o/9qdq0mfxjnMLOK3TQYYThh0IwDQYJKoZIhvcN\n" +
            "AQELBQADgYEALqUqIiE51O1aS/oktx9WmgW0daX5AZVXSWhS3yT65BeJP0rJVIwv\n" +
            "qb1teHi4skmtXW63dELhlWrltLqFxwN+ufzoQthITOFWCKz/KqGzNI3uI/pjs5fS\n" +
            "zOPu6CEqg8NOdPaU4E6H5ZVJ/v9m5uY/rCFyd+CKJaVg6q5rzI1khR8=\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIID/zCCAuegAwIBAgIGAWzZx56eMA0GCSqGSIb3DQEBCwUAMIGJMR0wGwYDVQQD\n" +
            "DBRSb290Q2EgTGJhYXMgRXhhbXBsZTEcMBoGA1UECwwTQ2xvdWQgTG9hZEJhbGFu\n" +
            "Y2luZzEaMBgGA1UECgwRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcMC1NhbiBB\n" +
            "bnRvbmlvMQswCQYDVQQIDAJUWDELMAkGA1UEBhMCVVMwHhcNMTkwODI4MTk1MDMz\n" +
            "WhcNMjEwODI3MTk1MDMzWjCBizELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAlRYMRQw\n" +
            "EgYDVQQHDAtTYW4gQW50b25pbzEaMBgGA1UECgwRUmFja3NwYWNlIEhvc3Rpbmcx\n" +
            "HDAaBgNVBAsME0Nsb3VkIExvYWRCYWxhbmNpbmcxHzAdBgNVBAMMFkxiYWFzIEV4\n" +
            "YW1wbGUgSW1kMSBjcnQwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOm3So1C\n" +
            "Pu5y+ZgBbYSMv3YZjISwt0qEzzUlVhEbtcCfWOVE+84qFeIVDZtIbHwywVuM8fOq\n" +
            "ppul7V9Jia9TlsbJ9HCAYcUnxB/YvhUyuZAontsQJItE+qBXsLTHnGEGnIKfK7jM\n" +
            "vhjShx2qzqzhtJVH9dh+sV7hP4I71qMF/F5TAgMBAAGjgewwgekwDwYDVR0TAQH/\n" +
            "BAUwAwEB/zCBtgYDVR0jBIGuMIGrgBQFwSdYA1adzGIslcp3YSLVVNH6aqGBj6SB\n" +
            "jDCBiTEdMBsGA1UEAwwUUm9vdENhIExiYWFzIEV4YW1wbGUxHDAaBgNVBAsME0Ns\n" +
            "b3VkIExvYWRCYWxhbmNpbmcxGjAYBgNVBAoMEVJhY2tzcGFjZSBIb3N0aW5nMRQw\n" +
            "EgYDVQQHDAtTYW4gQW50b25pbzELMAkGA1UECAwCVFgxCzAJBgNVBAYTAlVTggEB\n" +
            "MB0GA1UdDgQWBBSwHWg6UKongt+lUZbLWcPBhau+JjANBgkqhkiG9w0BAQsFAAOC\n" +
            "AQEAgi3AvT+xGk3XStMLfz6fmTjuiLlCDijcCLT6J4d8wrXCm7sbZ36UU3+jZNJp\n" +
            "X40VqVe3WmDz1kDeaZqhC/HGQYBxCItk1K16Dj0ury40rNlJyv/x+YpBQWR3KbBD\n" +
            "MbEfVEg5RIBQnueqP6srE/E3u7TH0hHpBlvK+RFSioTGDDOeiDhIo5E5XhSPOODD\n" +
            "py0amY6uuj5YIhokLvaBDvpPe3tjqSMNz6CxhIoorT/2wUNrBPfvvwghsMvXMwm6\n" +
            "UaST5gIukqHRgKa8jwR3k3bRajO7eDrBxvl2l4do+WrNy2CbgECOx6bpFEFkaNVy\n" +
            "aFOtsyoPWzOEz2z51DwOztI2JA==\n" +
            "-----END CERTIFICATE-----";
    public static final String CMAPPINGS_KEY = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICWwIBAAKBgQDp4ZSR02oYMNTbo1vub8FknrY+uKz172nmWd291HwM8PVQ14H5\n" +
            "oIvCGb7bOIehJ01z0CYCOOCXOWlKL6alLqh+kR9X29hNoy2dL/HFnvux3I0wo6nW\n" +
            "bHVtgUhMKzA5bXSvQH8fI8H50cxUV8K2dOIvPr7dE9w//b/ozVSOJUVfXQIDAQAB\n" +
            "AoGAAgodDQ5+aNqYKJgql5Ay7Ak0RqS+oWBmmIMlG2AsesJpKTzP3djoDxfBYdEC\n" +
            "4cVheAaTJkbPspHmGKcEuYEIKBogNooufhhhO5ITV3EZwn/HggULjUljYAGQ5NHv\n" +
            "GcO8yMFBOB8FlRYpjaSf/PL/6fU7MHihHgcn+Q69ZVGQ9hECQQD40vR57nOyehBl\n" +
            "M2kyXE5fCV+gUwmYvUWC3rXUREyLVULckYbTLNWFl+BZpp1bb07E8hiVYsQqhIYv\n" +
            "GCUH51K5AkEA8KBNtFIZKFLQ9yEf+k59dBrdFXLfODXaE/VllzywtRUfxkNujEq8\n" +
            "CoS45WoBz9LBgK1SICSHIA3Su5jihkjvxQJAAzJipbZkkiRzVXmFnnoFH791FRA8\n" +
            "DVRRMk8+Ms311cxwoXjVLJ1k2OSMo1o6p1QUxP7/RdwQ/SLavnwm7aURMQJAL0tC\n" +
            "mot8WFmWqsv+7gKZU8Wn6HxQdUrYVw3nbsdF8AF74ksEolxaAzM86BJi0scYs0Ld\n" +
            "nmWvjVRKNAcDluMlBQJAO6HzrNyPi64TBFSJesqRfHxXuph3K5q0e4mUqnQ8fiTr\n" +
            "FAZzX+dofDVdKXsqYgv05HUy3RQ2mBF/RU508SO/IA==\n" +
            "-----END RSA PRIVATE KEY-----";
    public static final String CMAPPINGS_CERT = ""
            + "-----BEGIN CERTIFICATE-----\n" +
            "MIIDfDCCAuWgAwIBAgIBADANBgkqhkiG9w0BAQsFADCBizELMAkGA1UEBhMCVVMx\n" +
            "CzAJBgNVBAgMAlRYMRQwEgYDVQQHDAtTYW4gQW50b25pbzEaMBgGA1UECgwRUmFj\n" +
            "a3NwYWNlIEhvc3RpbmcxHDAaBgNVBAsME0Nsb3VkIExvYWRCYWxhbmNpbmcxHzAd\n" +
            "BgNVBAMMFkxiYWFzIEV4YW1wbGUgSW1kMyBjcnQwHhcNMTkwODI4MTk1MDMzWhcN\n" +
            "MjEwODI3MTk1MDMzWjCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAlRYMRQwEgYD\n" +
            "VQQHDAtTYW4gQW50b25pbzEaMBgGA1UECgwRUmFja3NwYWNlIEhvc3RpbmcxHDAa\n" +
            "BgNVBAsME0Nsb3V0IExvYWRCYWxhbmNpbmcxHDAaBgNVBAMME2V4YW1wbGUucmFj\n" +
            "a2V4cC5vcmcwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOnhlJHTahgw1Nuj\n" +
            "W+5vwWSetj64rPXvaeZZ3b3UfAzw9VDXgfmgi8IZvts4h6EnTXPQJgI44Jc5aUov\n" +
            "pqUuqH6RH1fb2E2jLZ0v8cWe+7HcjTCjqdZsdW2BSEwrMDltdK9Afx8jwfnRzFRX\n" +
            "wrZ04i8+vt0T3D/9v+jNVI4lRV9dAgMBAAGjgfAwge0wDAYDVR0TAQH/BAIwADCB\n" +
            "vQYDVR0jBIG1MIGygBTN2E73ka49l2dToy2KNf02iZzyb6GBkaSBjjCBizELMAkG\n" +
            "A1UEBhMCVVMxCzAJBgNVBAgMAlRYMRQwEgYDVQQHDAtTYW4gQW50b25pbzEaMBgG\n" +
            "A1UECgwRUmFja3NwYWNlIEhvc3RpbmcxHDAaBgNVBAsME0Nsb3VkIExvYWRCYWxh\n" +
            "bmNpbmcxHzAdBgNVBAMMFkxiYWFzIEV4YW1wbGUgSW1kMiBjcnSCBgFs2cefDDAd\n" +
            "BgNVHQ4EFgQUaKPm4tzmwL2XMfeEvcs8gRuBGycwDQYJKoZIhvcNAQELBQADgYEA\n" +
            "lyUK1ixNGSPqTJP6ozFKpv1k2CnBIRHF6d2jcBwwPuwQjOKyYyMrzGP6aOLP/8g0\n" +
            "IvQapAFf+a/Ya3Yic3q9F6kNMY1caYw3QPJjpLT6Bg7b3rNZo13yyJdySGlA87Rn\n" +
            "M/6Rj1yPNz2SbDJPy5RfobASRPAHJR2Ai9jrjbkaDO0=\n" +
            "-----END CERTIFICATE-----";
}
