package org.rackspace.capman.tools.util;

import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.exceptions.PrivKeyDecodeException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderDecodeException;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderException;

public class PrivKeyReaderTest {

    // N E and D are the modulus, public and private exponents of the pkcs1
    // and pkcs2 files. pkcs1 and pkcs8 are the same key encoded in a differen't
    // way.
    public static final BigInteger N = new BigInteger("99675546580362630388047288418776497546266544929459382202058935599504667617414334140172749425887382259824160592926415237189152946580311836277111345900797978984411591142653581039172617197568536529428300533573629398538360218288731002978667356038717342815557755512971914108459228431700269551202672244605971289509");
    public static final BigInteger E = new BigInteger("65537");
    public static final BigInteger D = new BigInteger("62682575900072713868699832840860194544636302893369226209250606097288331342357971333034464422212224734980132668215958581953915338516577994420325419380738927356749871388340080582299407690720186068868573272526535670811712614048279201687465492856991784602414667635821170594273503959802822245953527147728281671425");
    public static final String pkcs1 = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICWwIBAAKBgQCN8VQgtq8dik08rrRyxCqXOf8JFPIZCdZUqmyqCbLguX8NuDc8\n"
            + "PzpiQ0iQRxdz3+2eplVsvgSlx4YifJoJCfQmPexca1Mv4ZD7gcJIXJpj5SjvSTpn\n"
            + "z/9jEly6GsLJqMmkO40bNvnrqA9W4//cjNiiFjCBqYRLSDffSq/C2V9VpQIDAQAB\n"
            + "AoGAWUNQwpngGlWlKOo9NIdEuYGRbyaq8TAMh4X9fBlJUqWUzw3wPoUMtErX0VPD\n"
            + "3e0Ow9yakB4XUiHdqsZtIA4QaSRC2Pwh8vuI4klzq84TEdKtZWJUnJnfFVMljbFP\n"
            + "EalZ9hMSNRE5yGjFizhkP3G5ZPBZifU8lGhvZrv/RpjGFwECQQDG5R3wjEL2naQA\n"
            + "ULNfvwO6ko9QHZL41/NCDncmQ3O3mFFpdo9Nd1UUNcQc3+WsYljtP3tIOBdnd2Do\n"
            + "D5ip3pQFAkEAtrIwT7NqG3AuaIxe2ZuI4MM4rlOPqphHj9ZmSwTDxV3mkq307U5m\n"
            + "zswR0++tZSiwFfTMsgf6WnLOPmmi/6INIQJAKOEinDRX9xRDhwUPORirjYdeCVtq\n"
            + "v9ay2traRnUKrauS4BhML+HdDMBYRI8IR7wXBvxLPHa66Lsyob/C4vcqeQJADmVE\n"
            + "AUO13KuePIazApqK0osPcE0NSPfuy5qtPMQHWQnpHS2v2vMOjsTtsrF0DECEf3Zm\n"
            + "mrCePXpGXgyc11/BgQJAJKoL7MfwIztcBRSxVH3h04mf2qee3k9L95IMYSwEZJVG\n"
            + "WCGZfhOTekCwVmGYr+H02XHxTu8P57b9AghYVfnG4w==\n"
            + "-----END RSA PRIVATE KEY-----\n";
    public static final String pkcs8 = "-----BEGIN PRIVATE KEY-----\n"
            + "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAI3xVCC2rx2KTTyu\n"
            + "tHLEKpc5/wkU8hkJ1lSqbKoJsuC5fw24Nzw/OmJDSJBHF3Pf7Z6mVWy+BKXHhiJ8\n"
            + "mgkJ9CY97FxrUy/hkPuBwkhcmmPlKO9JOmfP/2MSXLoawsmoyaQ7jRs2+euoD1bj\n"
            + "/9yM2KIWMIGphEtIN99Kr8LZX1WlAgMBAAECgYBZQ1DCmeAaVaUo6j00h0S5gZFv\n"
            + "JqrxMAyHhf18GUlSpZTPDfA+hQy0StfRU8Pd7Q7D3JqQHhdSId2qxm0gDhBpJELY\n"
            + "/CHy+4jiSXOrzhMR0q1lYlScmd8VUyWNsU8RqVn2ExI1ETnIaMWLOGQ/cblk8FmJ\n"
            + "9TyUaG9mu/9GmMYXAQJBAMblHfCMQvadpABQs1+/A7qSj1AdkvjX80IOdyZDc7eY\n"
            + "UWl2j013VRQ1xBzf5axiWO0/e0g4F2d3YOgPmKnelAUCQQC2sjBPs2obcC5ojF7Z\n"
            + "m4jgwziuU4+qmEeP1mZLBMPFXeaSrfTtTmbOzBHT761lKLAV9MyyB/pacs4+aaL/\n"
            + "og0hAkAo4SKcNFf3FEOHBQ85GKuNh14JW2q/1rLa2tpGdQqtq5LgGEwv4d0MwFhE\n"
            + "jwhHvBcG/Es8drrouzKhv8Li9yp5AkAOZUQBQ7Xcq548hrMCmorSiw9wTQ1I9+7L\n"
            + "mq08xAdZCekdLa/a8w6OxO2ysXQMQIR/dmaasJ49ekZeDJzXX8GBAkAkqgvsx/Aj\n"
            + "O1wFFLFUfeHTiZ/ap57eT0v3kgxhLARklUZYIZl+E5N6QLBWYZiv4fTZcfFO7w/n\n"
            + "tv0CCFhV+cbj\n"
            + "-----END PRIVATE KEY-----\n";
    public static final String caKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXQIBAAKBgQCDwGVJ6fe+iXCVzCcGWc4XcNgoa3R/2YW9fsV8FKfan/6UfeDx\n"
            + "Ywb7MeDtHg7VFawLLUMrStOEN2ZKcYsQxsMS65P4lrxrSMSU7F6HtVZMB+XVYe4U\n"
            + "eUSVo03MN8t6l9a3/A0bga16/SdBqZ91P9BO0IAtxcIyk2m/cYKopDrCwwIDAQAB\n"
            + "AoGAW1RlYmVzvXsctlp8uuRJ/unUjcBfU7kAAqn8T9Upvl2mZl0UL4CL+FlNKFHr\n"
            + "yj5psp2/sCUAluioWfZ3hjuiQV6R8ZnZW712YhXnov6Ph/r4NcMGnHJYer0HM1zq\n"
            + "VFlyPWQCkS34u+aQhPpKAdddBrLmjSv1DgxRMaWs6RYtMrECQQC+Nl/7afabGRKw\n"
            + "QihMzjSv1ZBsue3IH7uqNlQf0nW0u+YiDGheSxikzZulBK/lO9XxMGVtYhrwq/TG\n"
            + "lPAnU65/AkEAsVHURZAGzTydFv0dnMm8g/tknqt+OKlZBgHuBe5Zt+RV3jIIkt5V\n"
            + "UiNUS0YTUuZwa4+SJP5nN6J+58AHBeORvQJBAIka2o53L6lWJlFkLnZGQFXp44Nr\n"
            + "dYjFzth+9p5FblCLC/PI68Xj7WyFQ8ZrnXnnamvCjamNiIun9vTY0E4YlHMCQQCP\n"
            + "ri7C7yGTzDm+FvuXwB/xEhNGPs/YOeDY7VdhlvE8ANlTYldwKpgYJmh3ViDyW6dc\n"
            + "gMl7EGmyuwj54K/QJcZBAkBuUPuLnWSfMYw6fyguxH7oqzmSt8mMZBlbu7ocrBC1\n"
            + "pdzb+eyYlzihca/Wa0nBtn43wqUq415AAJKfnGUzqtSd\n"
            + "-----END RSA PRIVATE KEY-----\n";
    public static final String testKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXgIBAAKBgQCEG7HNhuX0VLKIYOQaghsA6HJSZ4nRACfxyzXSX3KyDlRYM9pG\n"
            + "FjJtoMg2usNEnKesfhOmCXfYehXMwj334T388You+LcdPEj2FKRE0kpo3c8VxgjC\n"
            + "VLgTHnQm77wt/MEGbChjAkJrzz2gk6YwQ6nJX51rjwvIx3Rr+tfb/i8xGQIDAQAB\n"
            + "AoGAcyxx5u0krc7pl1xhgXrMcA43HQCHdl7cdEDlu3LbW8CCaCNMuK3BaTIzWwOY\n"
            + "Gck5pXiFSMwYX/KP7uOpguIsV4gBrJhy3l6wQmhH5QGshqIBEDtbI21gTisv1fhx\n"
            + "DYceEqwp9WapRf719cyMMMAQ+8Nm1YDudUxeDnUvWfcoxcECQQDFjSdeVMxexFgE\n"
            + "0vKlPw0oP4+6sMvvaObEOFfW/e73LM0ome2XXSixQyA30z8y/1145rD/sNK4KiaQ\n"
            + "l9Ea0DOfAkEAqzHFmqxW860LffT2XyXYhpHzdQiJdm0WXUDkx/2FPXGyljqBGZtL\n"
            + "ZWYDUpE3HyOYCrCYZsMGbYusodGS/uMgRwJBAJLIocLWcQ/NBbV34/DiW21XZP0L\n"
            + "Vkwp/qU3VBUbks43jKypSr8X6h9jx/GS1beXxKULi+JASSGruAHhu+4XWvMCQQCA\n"
            + "+p7GSdG5BUcDPuvgA8N+n7etFSF79/RBjgLQKlGYWXETfkCF6lqDqrgWHRJKg6ap\n"
            + "ZyNrSMQvBGyr/hmhr71BAkEArMvC4eVA9MRXfpPclDgSpSGUpiklbaeZB/jbpi4Q\n"
            + "V0ozFm1Kecdu1Le+mp5Mad3qktlMB7Euifsj8QlT2KN5AQ==\n"
            + "-----END RSA PRIVATE KEY-----\n";

    public static final String key2048bit = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEpQIBAAKCAQEAo5ZrBNCUIeKU8gfptbq9CpkUeZwRasJmij4lGJBaRrTeOpeX\n"
            + "z6lMEqiN49+UvE3NF6LuMCx/sb+/gAU+BGS/pAauJ/TbIESHF4dDzfUMr5nOk0Ae\n"
            + "slfST3QX4wAgVDkuuxxHP0K+jL85qaYBGsLL4HETKMDZKXvOcCORSq0ZCy7D1sP0\n"
            + "wCHomtX7k1RE7EgWsKDOumj+7SH/9jnBMKrHNqtj3xBZt18tugSyfeZVwQgGOW+m\n"
            + "f7A96tLmw1XQgoAxQqTTZkjFIMjM5JEFOeX8Bia0CzixTMGN2BjnuafkoUxslaF4\n"
            + "APmRu6n4xGlDuDuKpAFHEYg2nyPkezpsBX5s7QIDAQABAoIBAQCWoBG6RTOgX7k1\n"
            + "ggO3yVH3SCyKLSH8YzN3ZvFRRNla8X8OBDdMhl39cCX2BBA3sot9kBAxW0fYqu3x\n"
            + "OuJ3uSycI2qIb/S0KWUaTPop0dD0f3KuMwQQwrxrXEICSkN6SYy1zLvti89YWVsG\n"
            + "0kuCEIsZBgTWKXvLrqvOpXFKiUfd+qBKJjPqV+7Thna5ffoznnE+fTPaKozKwknm\n"
            + "tdMd6pG7vdhht8/vEf6s0qCNRkStpZhbs0OjGCziqp6Zs0IzrmUdDP4QLiNlt1Fh\n"
            + "HLSkN9fuD3z6PanQ3DLcqeZTZEaZ6dSiJljS7VeUfYQbR8jhydqNzL8wbzIhH8l+\n"
            + "ZzL7KS0hAoGBANRcfXQpi/YAcdBeo2s/ugI2PxTlg/r2kaNFa03iJKYXvOHWS9eF\n"
            + "kNtEW5LfvJYWWQNz99e71pFksYqTqeEerJvB37xrluyIPjyOo2oxuRvQNCeTX5jR\n"
            + "XY4NFVwTWVaPYCC1zTK6y4jiOL+We44Kip6sgLS9kGZAA+gQnLRX1EmHAoGBAMU0\n"
            + "IWGq7sKqujGnhQUKfqay/4f6cAh0WEaGP5guqk9AMYT9IDQ98qVpMxh9uP7yJiTx\n"
            + "Mx5TDj7BBCIqpAt2OAFhMqovAqxKGOgiJU8BVacRF6ow8FOTXBwPcrdXHeDas+4n\n"
            + "DdFvjxa4+aeUK2SGNW8kPImEEOrxu3LEwSTIDSLrAoGBAKaiQKrC4xlQdf5cFH1W\n"
            + "jv2nVU5vXmWxzsu/8Bg4CCvwWn0Xa4GdQ/JaLEUOnOtkc8p62BKHSTHjQlEL13RX\n"
            + "XngF5Cr0fYPy0GsyPdZZV/gUIqifQpcmSfPqHkWWxTZf4L0qCu7wlj89y+vCCAeI\n"
            + "DAfAMmogiUtClg4l4uC8Pk7HAoGBAKkoBlpY3WVuPTjKkXe5gNpNQJPLZr5Zzj7w\n"
            + "eSx5Gu3QCqog1rb5TGJG0uV3MnC+Faoqm8avR9DckEcefIi4Z2IHlgYVPR28kZDN\n"
            + "eWNDqc0dBEegowWNqb0II0bRG3f9IcpvBZNZNkwvbzcoCfC4jq0/UA5Fkp11rWzN\n"
            + "CUAbuejxAoGAC+A33jMXrI8i+a/rHCtnyfG+mqbMV8+XOrXVH6jpVns8WmTGovPK\n"
            + "fQHzylx4AGlmn3X+zvahzEJZUvaClsQVaEJh0jykl4/48swkv3LNWrQVxPJGehk2\n"
            + "NYdiC1EDRLo2B+3mFOu8UbVz6e0q3KiWkLBxxDbMwzn7diCJdap6rDo=\n"
            + "-----END RSA PRIVATE KEY-----\n";

    public PrivKeyReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        RsaConst.init();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldGetCorrectAttrsFromKey() throws X509ReaderDecodeException, PrivKeyDecodeException {
        PrivKeyReader pkr = PrivKeyReader.newPrivKeyReader(pkcs1);
        Assert.assertEquals(N, pkr.getN());
        Assert.assertEquals(E, pkr.getE());
        Assert.assertEquals(D, pkr.getD());
    }

    @Test
    public void pkcs8ShouldMatchPkcs1() throws X509ReaderDecodeException, PrivKeyDecodeException {
        PrivKeyReader pk1 = PrivKeyReader.newPrivKeyReader(pkcs1);
        PrivKeyReader pk8 = PrivKeyReader.newPrivKeyReader(pkcs8);

        // Values should match
        Assert.assertEquals(N, pk8.getN());
        Assert.assertEquals(E, pk8.getE());
        Assert.assertEquals(D, pk8.getD());

        Assert.assertEquals(pk1.getN(), pk8.getN());
        Assert.assertEquals(pk1.getP(), pk8.getP());
        Assert.assertEquals(pk1.getQ(), pk8.getQ());
        Assert.assertEquals(pk1.getE(), pk8.getE());
        Assert.assertEquals(pk1.getD(), pk8.getD());
        Assert.assertEquals(pk1.getdP(), pk8.getdP());
        Assert.assertEquals(pk1.getdQ(), pk8.getdQ());
        Assert.assertEquals(pk1.getQinv(), pk8.getQinv());
    }

    public void pkcs8ShouldBeCodedbackToPkcs1ForZeus() throws PrivKeyDecodeException, PemException {
        PrivKeyReader pk8 = PrivKeyReader.newPrivKeyReader(pkcs8);
        String pkcs1Out = PemUtils.toPemString(pk8.getPrivKey());
        Assert.assertEquals(pkcs1, pkcs1Out);
    }
}
