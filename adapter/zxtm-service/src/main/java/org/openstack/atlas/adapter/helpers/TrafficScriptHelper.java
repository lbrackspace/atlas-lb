package org.openstack.atlas.adapter.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TrafficScriptHelper extends StmConstants {
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

    public static void addRateLimitScriptsIfNeeded(ZxtmServiceStubs serviceStubs) throws RemoteException {
        LOG.debug("Verifying that rate limit rules (traffic scripts) are properly configured...");

        boolean ruleRateLimitHttpExists = false;
        boolean ruleRateLimitNonHttpExists = false;
        String[] ruleNames = serviceStubs.getZxtmRuleCatalogService().getRuleNames();

        for (String ruleName : ruleNames) {
            if (ruleName.equals(ZxtmAdapterImpl.ruleRateLimitHttp.getName())) ruleRateLimitHttpExists = true;
            if (ruleName.equals(ZxtmAdapterImpl.ruleRateLimitNonHttp.getName())) ruleRateLimitNonHttpExists = true;
        }

        if (!ruleRateLimitHttpExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleRateLimitHttp.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleRateLimitHttp.getName()}, new String[]{TrafficScriptHelper.getHttpRateLimitScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleRateLimitHttp.getName()));
        }

        if (!ruleRateLimitNonHttpExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()}, new String[]{TrafficScriptHelper.getNonHttpRateLimitScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()));
        }

        LOG.debug("Rate limit rules (traffic scripts) verification completed.");
    }

    public static void addRateLimitScriptsIfNeeded(StingrayRestClient client) throws IOException, StingrayRestClientException {
        LOG.debug("Verifying that rate limit rules (traffic scripts) are properly configured...");

        boolean ruleRateLimitHttpExists = false;
        boolean ruleRateLimitNonHttpExists = false;

        List<Child> rules = new ArrayList<Child>();
        try {
            rules = client.getTrafficscripts();
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.debug("There was an error in StingrayRestClient: " + e);
        }

        for (Child ruleName : rules) {
            if (ruleName.getName().equals(StmConstants.RATE_LIMIT_HTTP)) ruleRateLimitHttpExists = true;
            if (ruleName.getName().equals(StmConstants.RATE_LIMIT_NON_HTTP)) ruleRateLimitNonHttpExists = true;
        }

        if (!ruleRateLimitHttpExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleRateLimitHttp.getName()));
            File createdRule = createRuleFile(StmConstants.RATE_LIMIT_HTTP, TrafficScriptHelper.getHttpRateLimitScript());

            try {
                client.createTrafficscript(StmConstants.RATE_LIMIT_HTTP, createdRule);
            } catch (StingrayRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in StingrayRestClient: " + e);
            }

            createdRule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleRateLimitHttp.getName()));
        }

        if (!ruleRateLimitNonHttpExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()));
            File createdRule = createRuleFile(StmConstants.RATE_LIMIT_NON_HTTP, TrafficScriptHelper.getNonHttpRateLimitScript());

            try {
                client.createTrafficscript(StmConstants.RATE_LIMIT_NON_HTTP, createdRule);
            } catch (StingrayRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in StingrayRestClient: " + e);
            }

            createdRule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleRateLimitNonHttp.getName()));
        }

        LOG.debug("Rate limit rules (traffic scripts) verification completed.");
    }

    public static void addXForwardedForScriptIfNeeded(ZxtmServiceStubs serviceStubs) throws RemoteException {
        LOG.debug("Verifying that the X-Forwarded-For rule (traffic script) is properly configured...");

        boolean ruleXForwardedForExists = false;
        String[] ruleNames = serviceStubs.getZxtmRuleCatalogService().getRuleNames();

        for (String ruleName : ruleNames) {
            if (ruleName.equals(ZxtmAdapterImpl.ruleXForwardedFor.getName())) ruleXForwardedForExists = true;
        }

        if (!ruleXForwardedForExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleXForwardedFor.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleXForwardedFor.getName()}, new String[]{TrafficScriptHelper.getXForwardedForHeaderScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleXForwardedFor.getName()));
        }

        LOG.debug("X-Forwarded-For rule (traffic script) verification completed.");
    }

    public static void addXForwardedPortScriptIfNeeded(ZxtmServiceStubs serviceStubs) throws RemoteException {
        LOG.debug("Verifying that the X-Forwarded-For rule (traffic script) is properly configured...");

        boolean ruleXForwardedPortExists = false;
        String[] ruleNames = serviceStubs.getZxtmRuleCatalogService().getRuleNames();

        for (String ruleName : ruleNames) {
            if (ruleName.equals(ZxtmAdapterImpl.ruleXForwardedPort.getName())) ruleXForwardedPortExists = true;
        }

        if (!ruleXForwardedPortExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", ZxtmAdapterImpl.ruleXForwardedPort.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleXForwardedPort.getName()}, new String[]{TrafficScriptHelper.getXForwardedPortHeaderScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleXForwardedPort.getName()));
        }

        LOG.debug("X-Forwarded-Port rule (traffic script) verification completed.");
    }

    public static void addXForwardedForScriptIfNeeded(StingrayRestClient client) throws IOException, StingrayRestClientException {
        LOG.debug("Verifying that the X-Forwarded-For rule (traffic script) is properly configured...");

        boolean ruleXForwardedForExists = false;
        List<Child> rules = new ArrayList<Child>();
        try {
            rules = client.getTrafficscripts();
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.debug("There was an error in StingrayRestClient: " + e);
        }

        for (Child ruleName : rules) {
            if (ruleName.getName().equals(StmConstants.XFF)) ruleXForwardedForExists = true;
        }

        if (!ruleXForwardedForExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", StmConstants.XFF));
            File crule = null;
            crule = createRuleFile(StmConstants.XFF, TrafficScriptHelper.getXForwardedForHeaderScript());

            try {
                client.createTrafficscript(StmConstants.XFF, crule);
            } catch (StingrayRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in StingrayRestClient: " + e);
            }

            crule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", StmConstants.XFF));
        }

        LOG.debug("X-Forwarded-For rule (traffic script) verification completed.");
    }

    public static void addXForwardedProtoScriptIfNeeded(ZxtmServiceStubs serviceStubs) throws RemoteException {
        LOG.debug("Verifying that the X-Forwarded-Proto rule (traffic script) is properly configured...");

        boolean ruleXForwardedForExists = false;
        String[] ruleNames = serviceStubs.getZxtmRuleCatalogService().getRuleNames();

        for (String ruleName : ruleNames) {
            if (ruleName.equals(ZxtmAdapterImpl.ruleXForwardedProto.getName())) ruleXForwardedForExists = true;
        }

        if (!ruleXForwardedForExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should exist...", ZxtmAdapterImpl.ruleXForwardedProto.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleXForwardedProto.getName()}, new String[]{TrafficScriptHelper.getXForwardedProtoHeaderScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleXForwardedProto.getName()));
        }

        LOG.debug("X-Forwarded-Proto rule (traffic script) verification completed.");
    }

    public static void addXForwardedProtoScriptIfNeeded(StingrayRestClient client) throws IOException, StingrayRestClientException {
        LOG.debug("Verifying that the X-Forwarded-Proto rule (traffic script) is properly configured...");

        boolean ruleXForwardedProtoExists = false;
        List<Child> rules = null;
        try {
            rules = client.getTrafficscripts();
        } catch (StingrayRestClientObjectNotFoundException e) {
            LOG.debug("There was an error in StingrayRestClient: " + e);
        }

        for (Child ruleName : rules) {
            if (ruleName.getName().equals(StmConstants.XFP)) ruleXForwardedProtoExists = true;
        }

        if (!ruleXForwardedProtoExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should always exist...", StmConstants.XFP));
            File crule = null;
            crule = createRuleFile(StmConstants.XFP, TrafficScriptHelper.getXForwardedProtoHeaderScript());

            try {
                client.createTrafficscript(StmConstants.XFP, crule);
            } catch (StingrayRestClientObjectNotFoundException e) {
                LOG.debug("There was an error in StingrayRestClient: " + e);
            }

             crule.delete();
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", StmConstants.XFP));
        }
    }

    public static File createRuleFile(String fileName, String fileText) throws IOException {
        File fixx = File.createTempFile(fileName, ".err");
        BufferedWriter out = new BufferedWriter(new FileWriter(fixx));
        out.write(fileText);
        out.close();
        return fixx;
    }

    public static void addForceHttpsRedirectScriptIfNeeded(ZxtmServiceStubs serviceStubs) throws RemoteException {
        LOG.debug("Verifying that the HTTPS Redirect rule (traffic script) is properly configured...");

        boolean ruleForceHttpsRedirectExists = false;
        String[] ruleNames = serviceStubs.getZxtmRuleCatalogService().getRuleNames();

        for (String ruleName : ruleNames) {
            if (ruleName.equals(ZxtmAdapterImpl.ruleForceHttpsRedirect.getName())) ruleForceHttpsRedirectExists = true;
        }

        if (!ruleForceHttpsRedirectExists) {
            LOG.warn(String.format("Rule (traffic script) '%s' does not exist. Adding as this should exist...", ZxtmAdapterImpl.ruleForceHttpsRedirect.getName()));
            serviceStubs.getZxtmRuleCatalogService().addRule(new String[]{ZxtmAdapterImpl.ruleForceHttpsRedirect.getName()}, new String[]{TrafficScriptHelper.getForceHttpsRedirectScript()});
            LOG.info(String.format("Rule (traffic script) '%s' successfully added. Do not delete manually in the future :)", ZxtmAdapterImpl.ruleForceHttpsRedirect.getName()));
        }

        LOG.debug("HTTPS Redirect rule (traffic script) verification completed.");
    }
}
