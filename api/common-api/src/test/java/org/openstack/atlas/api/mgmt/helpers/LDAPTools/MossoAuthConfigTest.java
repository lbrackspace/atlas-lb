package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MossoAuthConfigTest {

    private String uid;
    private String ctext;
    private String password;
    static String host;
    private int port = 636;

    public MossoAuthConfigTest() {
    }

    @Before
    public void setUp() throws GeneralSecurityException {
        uid = "uid=support-password-reset,ou=special users,dc=stabletransit,dc=com";
        ctext = "CxvjgwCGq0Ue82WJAa8HEg==";
        host = "127.0.0.1";
        password = decryptPasswd(ctext);
    }

    @Test
    public void testMossoAuthConfigFromFile() throws IOException, GeneralSecurityException {
        String fileName = "/tmp/ldap.conf";
        FileWriter fw = new FileWriter(fileName);
        PrintWriter pw = new PrintWriter(fw);
        pw.printf("host=\"%s\"\n", this.host);
        pw.printf("port=\"%d\"\n", this.port);
        pw.printf("connect=\"ssl\"\n");
        pw.printf("grouprole[\"ops\"]=\"DL_lbaas-dev,im_mosso\"\n");
        pw.close();
        fw.close();
        MossoAuthConfig tConf = new MossoAuthConfig(fileName);
        assertEquals(this.host, tConf.getHost());
        assertEquals(this.port, tConf.getPort());
    }

    private static String encryptPasswd(String passwd) throws GeneralSecurityException {
        return Aes.b64encrypt(passwd.getBytes(), "mossoFailure");
    }

    private static String decryptPasswd(String base64str) throws GeneralSecurityException {
        byte[] ptext = Aes.b64decrypt(base64str, "mossoFailure");
        return Aes.bytes2str(ptext);
    }
}
