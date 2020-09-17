package org.openstack.atlas.adapter.helpers;

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
    public static final String ENCRYPTION_KEY = "ppxb5fniQshy-U5yp-uJAXG2QTAmSrnHSlG56aO2iFrLTEMU0r1bhgm8TN25KRNezLxrN2JgxKkRu--Gx5R7Kw==";
    public static final String IV = "testiv";
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
    public static final String ENCRYPTED_SSL_KEY = "tdvaeR8dQLkz8324dzGNtlBCO5C" +
            "+rWQSKil5QgbYvoVa2TJxlzo5fsdiRT9hpNUwixdu4eqcMhSzHJp9J9XkO9f5chK3/" +
            "5lpWnfExlJQMZKxzQiMu6gX/2XV7Diuo7aY1frwde+Bq3rzOlxX+XwMKpOoooWG3qb" +
            "eyPPrRUYCE/qvxHxgaYjh5wENIMd4nF2vJyIcvcMieSbGopajHU0TjPnv+CREaHGqL" +
            "FT+dTppDraF6J64hxWoItyP0+XV2J0HfOFLm0VhXot+Td3als5jLZ5BmN80y+IIOyq" +
            "EXuicIHaOmQRslQrmM4zSPneNukFjoXVMghv7wANtldU9mrqDsFA5dlxUZdRhqPWFZ" +
            "+FQBOjDtxDECAXys6tvUlBHc6kYvyioc0LvsLG7tNlmpMT3Vq/9uFXacceWDQg2axr" +
            "5a/LbmAhBPqsk/8D7m5jagrcMsRlWKnYUBPiJdfNGQcAePcD8pIjEVfUyq+t2NTEw8" +
            "Vf6D+EGj3zWyLX6P0EBnyiCLl9bRn9+SFGjaPHFBm2tp8rB8oBiGTckm4BTVoBW/HW" +
            "z+XfYIWBO/BbmZvxpzjs9v6CtCjxfVEiEp7CSxfS7HQERv/hJLzZWtihWcAwFSXhb7" +
            "+a5DcJbW71RuDybH2cHF61ByKW11iVk7QJ3ncPTXqtp1qtsUCxv8htzc4w0GK3hTV1" +
            "dvX8g6OoHeO3NaaknSgNPVXDqwF5bdhZ+S4efxHLTaZarKf6psSGCgZ7dB4mUsc9mo" +
            "eKgFj+tDSHpJhS8mdrxYfE63QT51JpKFkAhBjQrW28o1YBSRBcIPw/nLlRlO8V2qLo" +
            "UgkuCp/0H7YYtVGYPSGI4ClVIkwZBwV0JL8GI/41L1PevFr6I8NZ/m/JWO2TVb/iIV" +
            "RRvxTGDiDztz10MbMTjHWI5MXr2hcRhxBj4+FL2UxP8nSGyH4COL+UwUwRg0RAcyT1" +
            "6POQA9D7VI/ZGYJCJue5gsn7Jl52MVTGDE0sCBQmLjl+0rx0TIMRJ+t/L4TBlDnMgu" +
            "CQyUBZ9UwUXKH/nrm++bhKEkBuiEy3n/tHjlhDD0zmrxcCYWSuYS+397/pYzFJO8sL" +
            "YiEGQOrr5LIHbEt6pshjI5bkHkbuIQG3+WuJEWb2RI8HOzb1WtRRSoWGikCA5ozY2c" +
            "v+F+s+eC1BkL047cQeGdE2L/jonDgAHaKv4aufEUtye3vy1diDl5PUknbKfPGLZ1aY" +
            "53kHgYCcwlEbM109R5vbkKksrWrmxGsxnubS6ksRBXaQ8WDu7zLXTlYka2YfNLV/sM" +
            "9IxzRV364KO8gU4ZE2SzUBr9HrzYYa+SKJdCZqFA2Vw12nUKYkdx4QX5Crx9++BeW9" +
            "3KldVmJBPYRtk2YAm9iuLLW0W4Z6qdBHzxzGyPjbJXr3Scqp9IK6QuCawWldpKQyo7" +
            "PYPFtbxfxdY+uQ0WYr58ULIvHHzpJHdkeM3s2Lm1neqfWthacBZmdXM+5QIS6XB7K+" +
            "2NpLZ08AwFjOZbPmpPw8j9ufjP1yjZLa/8cx+cJcuGBKgNQ+Whl2QqEnbXal7uv7ai" +
            "MdSpYcujBau8RexzQ3F6AHQbZiVWVEqAG60+LZLdCoR/GqdKTD1bNcLNUKYls3oFOe" +
            "k07V7LT98GQQ+ChWP7/Z+zE2HbJJAtbsrcRh//Ztal43L9cIw3/GmUTLYbegk3i58N" +
            "GVufnmilZaD5tO1Ag96aeuwTD+Q6jC+Riicu4Ux8PKPH2vowTiUF3Icpp+QTFp8ZaN" +
            "SKAIAluzTQ6Oc2kbfplj/4/aKoW6n3K54uimIiLSRIgpgfXpH+w5GNbOuylff9wM0v" +
            "o0j0uUWwd4L7YCAzjBktArs3q6O9EmoO5quaqLvbe4/zKO5KbX3arl2UePGSFuKhmh" +
            "xpyWUgh5t4HVX9qu0avPP+T08SoSUncpL3SjAYdvh6ZvUS5MGWcVXQzkWDDDhC1BQN" +
            "wtoJorBTLn2drkzKqtcoo9SLubMeEoY41MEgmKr4yroRtzF0+6CtkcI3v4TPKeCH2u" +
            "coaFuN63qXoafel45SDXcpWJF6J0NzFkgS4ej1sygYtxWDsN5b9qV+K8fRYCqL3MB1" +
            "ZeoBapBDkjn+qaBY0dDmZiBYHcxhI0zoYslnEDC9MHwdjg8I6Nh5pqtI51mhARgTUo" +
            "zvvtuz/zWxkdmKilfVvtYy3rNETAm27HfpKBOO9gENb8Iew8BKiv2hd2sqJ+lUdBqW" +
            "4zFHn+ApuJgA9a6dZ6LKEjag1vwGrRW7kZqFzfz3kzGnaqbsyaSQmB6";
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
}
