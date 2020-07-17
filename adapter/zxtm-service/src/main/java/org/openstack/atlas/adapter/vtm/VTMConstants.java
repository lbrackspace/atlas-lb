package org.openstack.atlas.adapter.vtm;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;

public class VTMConstants {

    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String RATE_LIMIT_HTTP = "RATE_LIMIT_HTTP";
    public static final String RATE_LIMIT_NON_HTTP = "RATE_LIMIT_NONHTTP";
    public static final String CONTENT_CACHING = "CONTENT_CACHING";
    public static final String XFF = "ADD_X_FORWARD_FOR_HEADER";
    public static final String XFP = "ADD_X_FORWARDED_PROTO;";
    public static final String XFPORT = "ADD_X_FORWARDED_PORT";
    public static final String HTTPS_REDIRECT = "FORCE_HTTPS_REDIRECT";
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String HTTP_COOKIE = "COOKIE";
    public static final String SSL_ID = "SSL_ID";
    public static final String X_FORWARDED_FOR_SCRIPT = "http.addHeader( \"X-Forwarded-For\", request.getRemoteIP() );\n" +
            "http.addHeader( \"X-Forwarded-Port\", request.getLocalPort() );";
    public static final String X_FORWARDED_PROTO_SCRIPT = "$vserver = connection.getVirtualServer();\n" +
            "if( string.endsWith( $vserver, \"_S\") ) {\n" +
            "        http.addHeader( \"X-Forwarded-Proto\", \"https\" );\n" +
            "} else {\n" +
            "        http.addHeader( \"X-Forwarded-Proto\", \"http\" );\n" +
            "}";
    public static final String X_FORWARDED_PORT_SCRIPT = "http.addHeader( \"X-Forwarded-Port\", request.getLocalPort() );";
    public static final String HTTP_RATE_LIMIT_SCRIPT =
            "$load_balancer = connection.getVirtualServer();\n" +
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
    public static final String NON_HTTP_RATE_LIMIT_SCRIPT =
            "$load_balancer = connection.getVirtualServer();\n" +
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