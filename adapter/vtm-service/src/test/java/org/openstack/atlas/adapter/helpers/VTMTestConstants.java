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
    public static final String ENCRYPTED_CERT_MAPPING_KEY_1 = "faM4Rr+NTLvRTTbc" +
            "/fiSG1n3aiTI5U2B5b2b200JH2NX5yqmDj9nRf3MB58prEY4vB+863ZKU2+2L5g5pq" +
            "Gxs/2d0yv1LJl5mReugnoOPEkCfBaqFwCJ4mIBTM4lQ+j6lR3jlX3MVmNU3l/d1cQM" +
            "IFOgJZnWadqVb8/ShIM6poSDM7uQmg6Go0XqgD5TNaFuOMzuS+hfpDMOzPD0q+VFeG" +
            "4JmTIR19gGYEUBsnaustmiB4sLHpworyq3hhTE1xBP6vEQ7H9BdWdZOPdhv2B6n761" +
            "XKnwUibTePV1OMKFngPvEXzMV1Oqc+vFy4aTopY+qN/VGh3AYQFdQsros4gIA9WohK" +
            "tyoRaKlooN17Y4VWx67806ZqsLsHniXKWkxxo1OqwbjiBxXSiL22zM+MnPNgU4Lby2" +
            "wqgWECQ/PGjt3MWct5AcewLezkWYi+dlj1qRE0N5OHoTJoWP0Kxo5hssrjrFWPi/qn" +
            "b0v/Z55lM/q3D04zc1x++CzCLBEce1jOOplnklTf/ruo2oCKxsLIv9EEy+2X5GygWO" +
            "vlc0ig/1l1Qrb+Kx9QEM9cyXp+Ey2mTU4aij32BuytM9d8NX61jsdyarBx8CA1YuLb" +
            "WZEd/RQryB9ieisD/a+knhjTJ0EJr/QMBixvBqKOzPH00LSYlnd2eY7fBiP+tbTZ8p" +
            "vIyJm/0Bk7QPDVIvMAzYTUhiFNVJejV0qQ7Q4tB3FkQcrWdjQzqvWJOvs+0rx+f22p" +
            "7TKeacng3qbgF3YJlzsz2wP9gykivt79iI0agp5IiGz5v47TKM6kz+4x6CJzNfVW26" +
            "Tt0HASl/EdaLDFpVMcgsuPIBtMnZm1n87PtLh6zh7XBKenEIn1qagHD9SQmKBx/VeJ" +
            "X0b2B5CeJp9G8o6kBYaXErpbb44CQ33fckoVvwqh5NB/WWh7r1FYyygr0nORrwVmtn" +
            "kUP16eSglE+QFVzuedddkR/KcKrmeo8ZkZH1Vb9PLan1au/SmarsjedjEJkivv2eiw" +
            "bOU1domA5OcE/fGxJl9FFtSxQcs/xtLFKckY5o3pJ2dtqZd2vQuZoAzdmnxRF/JSMv" +
            "7iQsxFyn7GYnKk/KXu4XRO/nvjkW1ITSisdFZ/S2FJ6o+lVonbf5OXukBXyd/2rnRW" +
            "leG8csEj17jhBfJ3Q3MoX1VNV5BhUThyZ9Zf7sq4LW5hAS2YNEkAjPrIRTMgYw0DSh" +
            "6HvDsW6KbXGvB7tbI1Eziky9WKXPxc36f3iONrZ99nrG8aXOBgnSX6EopiD0mdD8GS" +
            "5wTapajxhzF5fERx3H9+EYWZ9wjaBF2DsidV+D5MypkJg1jCVeny2SP3TdNV6ieEFn" +
            "VdaRtHCrbihRJ3ZUbHl2FsYqXXYd4/HD6jqsmlaDP/yFmGS5EAOYSFQOhqit6WhE45" +
            "mgewKFf0nMwPOcjEGGeLF1nTjnY5VK8tWO0vaOFCV1XHN+0LPG2uDAZJnHkiF0UV9j" +
            "wVSny8oqLr+p8zrHy3yyWrYaVrkdZbj9WyC0haWuy2c8rBQ6m2nEOU0n0MDneVou+k" +
            "Lr4PAIVDrbgrJyi+0V7ixeXki4uVINNQ6WokyDT+3spnYQ34jo7hYNuhGheQ28SqPG" +
            "qDRKuisvdYIDCXLCWcTRgN7zVs3wVTyHYW7S6xW+dh4zOLL0T3UHQ158wlF9bB2haD" +
            "qSYWXgwQB3HabYgnDpU91mcDZoaEvnbGHcyu6A2bEZnN/J+4yDhhotTTmPS2TKeB69" +
            "5gzWnZwrirscbzmar2LUaxghzEk5LhyEUUtXB5vBlF9jVQLiTBDwZv5D3ALoW5N+ZL" +
            "v2nw0PZnuxGRA4TXeyfqjZOvwbsnXQmJRtqaGMyGPpp8rIrZWq7Yw4bEllIy8Q2YRi" +
            "OcknWl0tDPqJe5Tqrf+p7xFumPHUfD3kCR05xW28XqpktX7YIYcUSX9GV0VMhA80Sg" +
            "dxR1d9fK9pJKrCFFOrh2MvenhwIwcX5tH6sBLQfJtReW+SH4/UJNGV40OiPglepMhd" +
            "VrE+096UGdFGw2o24/Ggi2XELuNhNp3czb8KOOVtH9HyWaNrEWfhuiGDLdqmf9p4LS" +
            "Rv3NMN9Mka0VgJ8saLf88XW6XflwAlndHrs69Qb/xMik2vrhSus0jgDYC5qfpwOZGA" +
            "/qGg9tW8jF3aFOU70EfWX6h+NB9wOpqCSqSNx5KdkeRIF/D98J4X/FqgLToTbpyLt+" +
            "Zh/Y/zgO3Zp92tP0olN64AhmY9vlQL8jq4TuzRmC2eGtVOJt9UHn24lvcPGtCSjfFA";
    public static final String ENCRYPTED_CERT_MAPPING_KEY_2 = "Zd9dNoin2NE0coAHrU8/TS7HXVhZp2rxLf5R9yPPCdMvAaORIQx05q2X4CAlRrYSNwFyB2CtREfS9A8dwvJfhLb8X1MXWS+iSYu9+ChPArioSoNBw/r7Tb5aOULikt1bu6d7+h37xXlRUhPd2xRvhpvp6FlUTh2Gcg4a8M+kCth54BHprku6GNoCOK+8cX7LZfkpKFh3ShQcSfn5TIPn+XxHJOz/vH9mKj6Irl+FZFrOXip2rMoMdZi7rzTEAdEgg4KMCQPxtgQMCLzuqkH7oYM5RSdlL1jPDIXBTtcNfpfptvtg3bL7OA1gObOSwWavr+k4wazdDM0z83rFPDz6+oqx0mzguX5GkRIxcM+UINqv0l+MUxDYzA5znPSXt44MTsRh99JS26lQdJSIYmqdM2Ud84ncLRvoRuvjEPHBZnvnoh2zc6z1hIYjIZ+7U4ThOVaH23+Ln1RTOVVV5PAsuTeMnKrFUweHBK3sAyHxl9eAT6QjWQ2HX+hmpT0hsk93m8XmKnfY/FFUe2W+150qFFWknt6CIFnJaWLkFxBNIkQatbE/0RQ+VfbylIgTFjLAjyAbQCicB30TaGyyUj42zjfbaJedwUlb3NyVF+emX1MKCAocJp8nOsDtBhqpH9N/jhP0kRjn66rmMxgTU6uniVDy3NiUpzB7UfhdxgsLx7pI5JrT/HmcpsGo8aCLpG1VO1lyyD/Oie0cot/CTBG67rGZ9oQsVyyBsvrivORfB3Hq55yS1DgnC9IrGmVvkbc/0OlGdUUUvYr9vT9o/1Wn+lB//Y80mLo265TSzbZDOuNaRD3b+kXOeA41J5pIQP9qjJNYi8fvF2Km3YK1w/Pc2FKNWnKtpsXqyKoHCOUTzbEqOz182lrlTqHYmH+fLWI2YXdK0AnGBfgvl8d7EARY93zB7YnQrSlXsbnFnf3/B5ciR9RSHyqaH69xTVDjB5qiPoY+t3gtK7pZq19tQ80ZdQDIBIWnSAUB5nbIue0rpuEt6HIu96gH4U+pDY9KoBAxmadvaX0H9vDaETwpfHwvpygWlDuLccxy44M33KPUD12DtRrxeJltVpUyfdzEe3gRzjicc4SaGCoIiS+Rb1CKconelfkMD4mAmuX/JLhCwcCSDKfC3jvF0gcp9tcFLMHNjT1eSWCRxWiA++7e9JomLDUDYNwPHEm75sQOC1CnJN2LiG8PHz9xLWt+f6I6x7Oy3q+ANtYewCOpdyj+VigjGaXl7cFdXtOREuEjq4+dT0W32RhvcACqrDK/INp38cB0QRFH6P8Cx6Pavo+tU8EemKeIiAZphK6rf092QQwkEqLJlx+ztTwvcuObgW2NHfqarCPbK10TFgFbgez/0MN8zEROi9pL7xrxibjuyRZC/FXPozxo1TC0XdJ5gxdh79UkSpwKuSaC0qUCH08skKM9cmDFnRIcZ0aD4h0bIKKhbgQerX2eiUCQ1I/7RatZ7RjdJqUX238e75sSayyI8WSVpXzWi/cUIvxHiF9DHyl8w0Bm+IRLvGj64G0ZJl8sfQDupTJ+Lw1A0SeghxTSxAprXYQSc8vOCsYjFEJbC7ltpCkarvBuIcOMlTtQ+6qj6G/qPkKCoRwRKX+1kIAn64JIeNugJ7llFSJlugPCG0VT13kvUUZu470qiYBkXEUGPBBOPNHxiAdVavu8TNUieaZ7f1njDhG5opRRqTTOmHe7CSZrhpr/epERziEKlvPUYu+or7hI9vFZWNy6CH7WTBSs9yw68ogGnEAaHM60gYlJYRiWMaMx5ufeh5ptt+iGWbijFUuAPbnmzL2IxXacMI9HbKGB4HgTwAsBx7+VW9kKGFMbzuJHkrzV3ATin5o/IpMwbPRhIIlES8ZoEwICmAB4oChl36OStnCu8y4otAdvJ+A20JNGPMm4L0rrmfeGUGyuqjaYTOLyN71upz/itvMDvjtKgnI9fXAJBASebyRlqgftRsHU5FdcwdeqP8tLZomOicYy2y2s9YaZC6FFcjkFh+wchveQeZTyZhvVbPMWT4HIq4i2XmroGIBEF6x2lfkeHXF8Xukbr0GVIWgijbWDC1TfPf+Z0Q3jECngDqT/gMS2HGgs6iB0TGVh/H58KyPS6qsfVRLgJ2o5U3yNqjQcuIQLGZLyUlurJqWumH9CzbRqyZ7xcYuEK0xPJSd7fd0PTS/Cnv0zXO4oF8acVG+sz+H+pOrUYnQM9hhY+lo0UDLw48l1KJ99v3jn0Li2P/zwF75zuFHyQlRqkpmSXk4+";
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
