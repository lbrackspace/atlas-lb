package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
            System.out.printf("Attach your debugger if you want to and Press Enter to continue\n");
            cmdLine = stdin.readLine();
            conf = JsonConfig.readConfig(jsonFile);
            Map<String, ClassConfig> classConfigMap = new HashMap<String, ClassConfig>();
            Map<String, GroupConfig> groupConfigMap = new HashMap<String, GroupConfig>();
            classConfigMap.put("user", conf.getClassConfig());
            groupConfigMap.put("groups", conf.getGroupConfig());
            mossoAuth = new MossoAuth(conf.getMossoAuthConfig());

            // Test if the user is in ldap
            user = conf.getUser();
            password = conf.getPassword();

            if (mossoAuth.testAuth(user, password)) {
                System.out.printf("GOOD login %s\n", user);
            } else {
                System.out.printf("ERROR login %s\n", user);
            }

            Set<String> groups = mossoAuth.getGroups(user, password);
            List<String> groupList = new ArrayList<String>();
            for (String groupName : groups) {
                groupList.add(groupName);
            }
            Collections.sort(groupList);
            int nGroups = groupList.size();
            System.out.printf("\n");
            System.out.printf("user %s is a member of %d groups\n", user, nGroups);
            System.out.printf("------------------------------------------\n");
            for (String groupName : groupList) {
                System.out.printf("%s\n", groupName);
            }
            
            ArrayList<String> matchedRoles = new ArrayList<String>();
            for(String groupName : groupList){
                if(conf.getRoles().containsKey(groupName)){
                    String roleName = conf.getRoles().get(groupName);
                    matchedRoles.add(groupName);
                }
            }
            Collections.sort(matchedRoles);
            int nRoles = matchedRoles.size();
            System.out.printf("User %s is in %d roles\n", conf.getUser(), nRoles);
            System.out.printf("----------------------------------------------------\n");
            for(String roleName : matchedRoles){
                System.out.printf("%s\n", roleName);
            }
        } catch (Exception ex) {
            String msg = Debug.getEST(ex);
            System.out.printf("Exception caught\n%s\n", msg);
        }
    }
}
