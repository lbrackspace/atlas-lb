package org.rackspace.vtm.client.integration;

import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.config.ClientConfigKeys;
import org.rackspace.vtm.client.config.VTMRestClientConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class VTMTestBase {

    public final String TESTNAME = "i_test";
    public VTMRestClient client;
    public VTMRestClientConfiguration config;


    public void standUp() throws DecryptException {
        config = new VTMRestClientConfiguration();
        String adminUser = config.getString(ClientConfigKeys.stingray_admin_user);
        String adminKey = CryptoUtil.decrypt(config.getString(ClientConfigKeys.stingray_admin_key));
        URI endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint) + config.getString(ClientConfigKeys.stingray_base_uri));
        client = new VTMRestClient(endpoint, adminUser, adminKey);
    }

    public File createTestFile(String fileName, String fileText) throws IOException {
        File fixx = new File(fileName);
        FileWriter fw = new FileWriter(fixx);
        fw.write(fileText);
        fw.close();
        return fixx;
    }

    public static void removeTestFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            } else {
                System.out.println("File " + fileName + " delete operation is failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
