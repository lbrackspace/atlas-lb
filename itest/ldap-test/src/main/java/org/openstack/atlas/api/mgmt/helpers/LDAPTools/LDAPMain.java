package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class LDAPMain {

    public static void main(String[] MainArgs) throws IOException {
        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);
        MossoAuth mossoAuth;
        JsonConfig conf;
        String cmdLine;
        File jsonFile;
        String user;
        String password;
        if (MainArgs.length < 1) {
            System.out.printf("Usage is <configJsonFile>\n");
            System.out.printf("\n");
            System.out.printf("runs the snmpCommandline tester using the jsonConfig from the\n");
            System.out.printf("specified file. Example configuration\n%s\n", JsonConfig.getExampleJson());
            return;
        }
        try {
            jsonFile = new File(StaticFileUtils.expandUser(MainArgs[0]));
            System.out.printf("Press Key to continue\n");
            cmdLine = stdin.readLine();
            conf = JsonConfig.readConfig(jsonFile);
            Map<String, ClassConfig> classConfigMap = new HashMap<String, ClassConfig>();
            Map<String, GroupConfig> groupConfigMap = new HashMap<String, GroupConfig>();
            classConfigMap.put("user", conf.getClassConfig());
            groupConfigMap.put("groups", conf.getGroupConfig());
            mossoAuth = new MossoAuth(conf.getMossoAuthConfig(), groupConfigMap, classConfigMap);

            // Test if the user is in ldap
            user = conf.getUser();
            password = conf.getPassword();

            if (mossoAuth.testAuth(user, password)) {
                System.out.printf("was able to bind as iser %s\n", user);
            } else {
                System.out.printf("Unable to bind as user %s\n", user);
            }
        } catch (Exception ex) {
            String msg = Debug.getEST(ex);
            System.out.printf("Exception caught\n%s\n", msg);
        }
    }
}
