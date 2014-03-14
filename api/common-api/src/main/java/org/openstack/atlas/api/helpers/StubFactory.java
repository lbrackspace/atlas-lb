package org.openstack.atlas.api.helpers;

import org.openstack.atlas.docs.loadbalancers.api.v1.X509Description;
import org.openstack.atlas.service.domain.services.helpers.X509Helper;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;

public class StubFactory {

    private static final String x509strExample = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIEBzCCAu+gAwIBAgIBATANBgkqhkiG9w0BAQsFADCBrDE9MDsGA1UEAww0YXBp\n"
            + "LnJhY2tzcGFjZWNsb3VkLmNvbS9lbWFpbEFkZHJlc3M9cm9vdEBub3doZXJlLmNv\n"
            + "bTEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2luZzEaMBgGA1UEChMRUmFja3Nw\n"
            + "YWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhh\n"
            + "czELMAkGA1UEBhMCVVMwHhcNMTQwMzEzMjExODExWhcNMjQwMzEwMjExODExWjCB\n"
            + "rDE9MDsGA1UEAww0YXBpLnJhY2tzcGFjZWNsb3VkLmNvbS9lbWFpbEFkZHJlc3M9\n"
            + "cm9vdEBub3doZXJlLmNvbTEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2luZzEa\n"
            + "MBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlv\n"
            + "MQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwggEiMA0GCSqGSIb3DQEBAQUA\n"
            + "A4IBDwAwggEKAoIBAQCYT8tuAtvCCIQFixyeYas8OfsfsptV9xYXHzjuoKZ1i48b\n"
            + "i98FRLI07ewEgnZC70IW1txS0tp3eUPYvhZBeySNwnf+ltNBSqieMYrlEelQynSt\n"
            + "/bW/leOFpRpoNM19LGU34fuHkGyxRdfV1dRPnZy1EBYf1gFtT8in9WHHL/67pcBE\n"
            + "aD/EpsFf30/XjMwh8nApo66ujXWb7mxbBCmlE+ijxhBimbRbbkS1Lj1/RyuVBQau\n"
            + "cX+FUnwgrFyOzjwybdOmk2cG8PWKA6700BH9164ksZ0U4eBOPN+C55z/JDZxyNGU\n"
            + "zxKT90B/HfG7GtBH5bCuV2/5eP1r2tWHbztkO2tFAgMBAAGjMjAwMA8GA1UdEwEB\n"
            + "/wQFMAMBAf8wHQYDVR0OBBYEFEF8WZz8J6L0mmIcvU3AX8Nr1Ko4MA0GCSqGSIb3\n"
            + "DQEBCwUAA4IBAQAIyoTMvbFlv/YYdPZTD49L2ByFS2kp7j8yhtVpohHf0vJI6fuD\n"
            + "xj2O1WY+O0B+lUh25eX841gYWeJpKnR3qsLSzFPEn+Zw9nAwt0F0YLfUZddNeNLn\n"
            + "Kb2LY0uk0/MY4uTqlAAHJiiFNQuTvRTCfSWiG9CGHgZxT54B0xLSqSeZMJz28TWJ\n"
            + "RPAhHTV1HRn52HrNDYebM1Y8gywXajBJKHu2j+75SjFphWJNL9QY6SWHFzP2eoyO\n"
            + "niaxErd9DX0WoX+3KE5HPjL487bhKDyzBeTrc+/HXZejvE9H1mp7vVYxZ9nzb+/4\n"
            + "UkqrR7dL9pfouT4OQ1OChGlj17iDzRjIQUsy\n"
            + "-----END CERTIFICATE-----\n"
            + "";

    public static X509Description newX509Description() throws X509ReaderException {
        X509Description x509des = X509Helper.toX509Description(x509strExample);
        return x509des;
    }
}
