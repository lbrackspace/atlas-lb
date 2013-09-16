package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;

public class StmConstants {

    public static final LoadBalancerAlgorithm DEFAULT_ALGORITHM = LoadBalancerAlgorithm.RANDOM;
    public static final String RATE_LIMIT_HTTP = "rate_limit_http";
    public static final String RATE_LIMIT_NON_HTTP = "rate_limit_nonhttp";
    public static final String CONTENT_CACHING = "content_caching";
    public static final String XFF = "add_x_forwarded_for_header";
    public static final String XFP = "add_x_forwarded_proto";
    public static final String SOURCE_IP = "ip";
    public static final String HTTP_COOKIE = "cookie";
    public static final String X_FORWARDED_FOR_SCRIPT = "http.addHeader( \"X-Forwarded-For\", request.getRemoteIP() );\n" +
            "http.addHeader( \"X-Forwarded-Port\", request.getLocalPort() );";
    public static final String X_FORWARDED_PROTO_SCRIPT = "$vserver = connection.getVirtualServer();\n" +
            "if( string.endsWith( $vserver, \"_S\") ) {\n" +
            "        http.addHeader( \"X-Forwarded-Proto\", \"https\" );\n" +
            "} else {\n" +
            "        http.addHeader( \"X-Forwarded-Proto\", \"http\" );\n" +
            "}";
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