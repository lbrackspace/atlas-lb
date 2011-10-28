package org.openstack.atlas.adapter.zxtm.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;

import java.rmi.RemoteException;

public class TrafficScriptHelper {
    public static Log LOG = LogFactory.getLog(TrafficScriptHelper.class.getName());

    public static String getHttpRateLimitScript() {
        return "$load_balancer = connection.getVirtualServer();\n" +
                "$rate_html = \"<html><head></head><body><b>Error: 503 - Service Unavailable!</body></html>\";\n" +
                "$error_html = \"<html><head></head><body><b>Error: 500 - Internal Server Error!</body></html>\";\n" +
                "\n" +
                "$rate = rate.use.noQueue( $load_balancer );\n" +
                "\n" +
                "if ( $rate == 0 ) {\n" +
                "   \n" +
                "   http.sendResponse( \"503 Service Unavailable\",\n" +
                "                      \"text/html\", $rate_html,\n" +
                "                      \"Retry-After: 30\" );\n" +
                "   connection.discard();\n" +
                "   \n" +
                "} else if ( $rate < 0 ) {\n" +
                "   \n" +
                "   http.sendResponse( \"500 Internal Server Error\",\n" +
                "                      \"text/html\", $error_html, \"\" );\n" +
                "   log.error( \"Rate class: \" . $load_balancer . \" does not exist!\" );\n" +
                "   connection.discard();\n" +
                "   \n" +
                "} else {\n" +
                "   \n" +
                "   rate.use($load_balancer);\n" +
                "   \n" +
                "}";
    }

    public static String getNonHttpRateLimitScript() {
        return "$load_balancer = connection.getVirtualServer();\n" +
                "\n" +
                "$rate = rate.use.noQueue( $load_balancer );\n" +
                "\n" +
                "if ( $rate == 0 ) {\n" +
                "\n" +
                "   connection.discard();\n" +
                "   \n" +
                "} else if ( $rate < 0 ) {\n" +
                "\n" +
                "   log.error( \"Rate class: \" . $load_balancer . \" does not exist!\" );\n" +
                "   connection.discard();\n" +
                "   \n" +
                "} else {\n" +
                "   \n" +
                "   rate.use( $load_balancer );\n" +
                "   \n" +
                "}";
    }

    public static String getXForwardedForHeaderScript() {
        return "http.addHeader( \"X-Forwarded-For\", request.getRemoteIP() );";
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
}
