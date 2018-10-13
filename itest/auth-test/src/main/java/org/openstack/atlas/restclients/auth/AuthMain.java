package org.openstack.atlas.restclients.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.openstack.atlas.restclients.auth.client.IdentityClient;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.identity.client.token.AuthenticateResponse;

public class AuthMain {

    public static void main(String[] mainArgs) throws IOException {

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in);

        IdentityClient identityClient;

        if (mainArgs.length < 1) {
            printUsage();
            return;
        }

        String command = mainArgs[0];
        try {
            identityClient = new IdentityClient(mainArgs[1]);


            if (command.equals("authenticate")) {
                String password = readPasswd("Enter password: ", stdin);
                try {
                    AuthenticateResponse response = identityClient.authenticateUsernamePassword(mainArgs[2], password);
                    System.out.printf("User Authenticated, token: ");
                    System.out.printf(response.getToken().getId() + "\n");
                } catch (IdentityFault ie) {
                    System.out.printf("User not authenticated, reason: ");
                    System.out.printf(ie.getMessage() + "\n");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("validate")) {
                try {
                    identityClient.validateToken(mainArgs[2], mainArgs[3], mainArgs[4]);
                    System.out.printf("Token validated \n");
                } catch (IdentityFault ie) {
                    System.out.printf("Token not validated, reason: ");
                    System.out.printf(ie.getMessage() + "\n");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                printUsage();
            }

        } catch (IdentityFault identityFault) {
            System.out.printf("Failed to initialize auth client: \n");
            identityFault.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.printf("Usage: <cmd> <args> \n");
        System.out.printf("- authenticate <auth_url> <username> \n");
        System.out.printf("- validate <auth_url> <admintoken> <usertoken> <tenantid> \n");
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
