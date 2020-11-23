package org.openstack.atlas.adapter.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrafficScriptHelper extends VTMConstants {
    public static Log LOG = LogFactory.getLog(TrafficScriptHelper.class.getName());

    public static String getHttpRateLimitScript() {
        return HTTP_RATE_LIMIT_SCRIPT;
    }

    public static String getNonHttpRateLimitScript() {
        return NON_HTTP_RATE_LIMIT_SCRIPT;
    }

    public static String getXForwardedForHeaderScript() {
        return X_FORWARDED_FOR_SCRIPT;
    }

    public static String getXForwardedProtoHeaderScript() {
        return X_FORWARDED_PROTO_SCRIPT;
    }

    public static String getXForwardedPortHeaderScript() {
        return X_FORWARDED_PORT_SCRIPT;
    }

    public static String getForceHttpsRedirectScript() {
        return "if( http.headerExists( \"Host\" ) ) {\n" +
                "   http.changeSite( \"https://\" . http.getHostHeader() );\n" +
                "} else {\n" +
                "   http.changeSite( \"https://\" . request.getDestIP() );\n" +
                "}";
    }


    public static void addXForwardedForScriptIfNeeded(VTMRestClient client) throws IOException, VTMRestClientException {
        LOG.debug("Verifying that the X-Forwarded-For rule (traffic script) is properly configured...");

        boolean ruleXForwardedForExists = false;
        List<org.rackspace.vtm.client.list.Child> rules = new ArrayList<>();
        try {
            rules = client.getTrafficscripts();
        } catch (VTMRestClientObjectNotFoundException e) {
            LOG.debug("There was an error in VTMRestClient: " + e);
        }

        for (org.rackspace.vtm.client.list.Child ruleName : rules) {
            if (ruleName.getName().equals(VTMConstants.XFF)) ruleXForwardedForExists = true;
        }

        if (!ruleXForwardedForExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", VTMConstants.XFF));
            File crule = null;
            crule = createRuleFile(VTMConstants.XFF, TrafficScriptHelper.getXForwardedForHeaderScript());

            try {
                client.createTrafficscript(VTMConstants.XFF, crule);
            } catch (VTMRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in VTMRestClient: " + e);
            }

            crule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", VTMConstants.XFF));
        }

        LOG.debug("X-Forwarded-For rule (traffic script) verification completed.");
    }

    public static void addXForwardedProtoScriptIfNeeded(VTMRestClient client) throws IOException, VTMRestClientException {
        LOG.debug("Verifying that the X-Forwarded-Proto rule (traffic script) is properly configured...");

        boolean ruleXForwardedProtoExists = false;
        List<org.rackspace.vtm.client.list.Child> rules = null;
        try {
            rules = client.getTrafficscripts();
        } catch (VTMRestClientObjectNotFoundException e) {
            LOG.debug("There was an error in VTMRestClient: " + e);
        }

        for (org.rackspace.vtm.client.list.Child ruleName : rules) {
            if (ruleName.getName().equals(VTMConstants.XFP)) ruleXForwardedProtoExists = true;
        }

        if (!ruleXForwardedProtoExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", VTMConstants.XFP));
            File crule = null;
            crule = createRuleFile(VTMConstants.XFP, TrafficScriptHelper.getXForwardedProtoHeaderScript());

            try {
                client.createTrafficscript(VTMConstants.XFP, crule);
            } catch (VTMRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in StingrayRestClient: " + e);
            }

             crule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", VTMConstants.XFP));
        }
    }

    public static File createRuleFile(String fileName, String fileText) throws IOException {
        File fixx = File.createTempFile(fileName, ".err");
        BufferedWriter out = new BufferedWriter(new FileWriter(fixx));
        out.write(fileText);
        out.close();
        return fixx;
    }
}
