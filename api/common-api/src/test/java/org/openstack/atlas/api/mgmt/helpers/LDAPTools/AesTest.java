package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.security.GeneralSecurityException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class AesTest {

    public AesTest() {
    }

    @Test
    public void shouldEncryptStringCorrectly() throws GeneralSecurityException{
        String ptext = "ptext";
        String key = "TestKey1";
        String ctext = Aes.b64encrypt(ptext.getBytes(), key);
        assertEquals("XRpgF09fUzZxyFfn75bmIw==",ctext);
        assertEquals(ptext,new String(Aes.b64decrypt(ctext, key)));
    }

    @Test
    public void shouldDecryptStringCorrectly() throws GeneralSecurityException{
        String key = "TestKey2";
        String ctext = "dU7j+JrWwvySLDxFEvHOHw==";
        assertEquals("ptext",new String(Aes.b64decrypt(ctext, key)));
        assertEquals(ctext,Aes.b64encrypt("ptext".getBytes(), key));
    }

}