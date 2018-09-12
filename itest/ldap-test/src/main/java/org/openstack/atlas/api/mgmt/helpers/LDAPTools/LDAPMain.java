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
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class LDAPMain {

    private static final String exampleJson = ""
            + "{\n"
            + "  \"groupConfig\": {\n"
            + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
            + "    \"memberField\": \"memberOf\", \n"
            + "    \"sdn\": \"cn\", \n"
            + "    \"userQuery\": \"(uid=%s)\", \n"
            + "    \"objectClass\": \"(objectClass=*)\"\n"
            + "  }, \n"
            + "  \"appendtoname\": \"@rackspace.corp\", \n"
            + "  \"roles\": {\n"
            + "    \"support\": \"lbaas_support\", \n"
            + "    \"cp\": \"lbaas_cloud_control\", \n"
            + "    \"billing\": \"legacy_billing\", \n"
            + "    \"ops\": \"lbaas_ops\"\n"
            + "  }, \n"
            + "  \"isactivedirectory\": true, \n"
            + "  \"userConfig\": {\n"
            + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
            + "    \"sdn\": \"uid\"\n"
            + "  }, \n"
            + "  \"host\": \"10.12.99.71\", \n"
            + "  \"connect\": \"ssl\", \n"
            + "  \"scope\": \"subtree\", \n"
            + "  \"port\": 636\n"
            + "}\n"
            + "";

    public static void main(String[] mainArgs) throws IOException {
        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);
        MossoAuth mossoAuth;
        MossoAuthConfig conf;
        String cmdLine;
        File jsonFile;
        String user;
        String password;
        String conf_filename;
        if (mainArgs.length < 1) {
            System.out.printf("Usage is <configJsonFile>\n");
            System.out.printf("\n");
            System.out.printf("runs the snmpCommandline tester using the jsonConfig from the\n");
            System.out.printf("specified file. Example configuration\n%s\n", exampleJson);
            return;
        }
        conf_filename = mainArgs[0];
        try {
            jsonFile = new File(StaticFileUtils.expandUser(conf_filename));
            conf = new MossoAuthConfig(conf_filename);
            mossoAuth = new MossoAuth(conf);
            System.out.printf("Enter username: ");
            System.out.flush();
            user = StaticStringUtils.stripCRLF(stdin.readLine());
            password = readPasswd("Enter password: ", stdin);

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
            for (String groupName : groupList) {
                if (conf.getRoles().containsKey(groupName)) {
                    String roleName = conf.getRoles().get(groupName);
                    matchedRoles.add(groupName);
                }
            }
            Collections.sort(matchedRoles);
            int nRoles = matchedRoles.size();
            System.out.printf("User %s is in %d roles\n", user, nRoles);
            System.out.printf("----------------------------------------------------\n");
            for (String roleName : matchedRoles) {
                System.out.printf("%s\n", roleName);
            }
        } catch (Exception ex) {
            String msg = Debug.getEST(ex);
            System.out.printf("Exception caught\n%s\n", msg);
        }
    }

    // If there is a console attached to the JVM
    // use it to supress echo on password characters
    private static String readPasswd(String prompt, BufferedReader backupReader) throws IOException {
        String passwd_string;
        if (System.console() != null) {
            char[] passwd_chars = System.console().readPassword(prompt);
            passwd_string = new String(passwd_chars);
        } else {
            System.out.printf(prompt);
            System.out.flush();
            passwd_string = backupReader.readLine();
        }
        return passwd_string;
    }
}
