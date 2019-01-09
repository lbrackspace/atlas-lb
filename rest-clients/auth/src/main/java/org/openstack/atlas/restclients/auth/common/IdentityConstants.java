package org.openstack.atlas.restclients.auth.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class IdentityConstants {
    private static final Log logger = LogFactory.getLog(IdentityConstants.class);

    //TODO: clean this up a bit, find better way to do this?

    /** HTTP Header token that identifies Accepted status **/
    public static final int ACCEPTED = 200;
    /** HTTP Header token that identifies Create status **/
    public static final int CREATED = 201;
    /** HTTP Header token that identifies OK status **/
    public static final int OK = 202;
    /** HTTP Header token that identifies OK status **/
    public static final int NON_AUTHORATIVE = 203;
    /** HTTP Header token that identifies OK status **/
    public static final int NO_CONTENT = 204;
    /** HTTP Header token that identifies BadRequest status **/
    public static final int BAD_REQUEST = 400;
    /** HTTP Header token that identifies UnAuthorized status **/
    public static final int UNAUTHORIZED = 401;
    /** HTTP Header token that identifies Forbidden status **/
    public static final int FORBIDDEN = 403;
    /** HTTP Header token that identifies NotFound status **/
    public static final int NOT_FOUND = 404;
    /** HTTP Header token that identifies MethodNotFound status **/
    public static final int NOT_PERMITTED = 405;
    /** HTTP Header token that identifies NameConflict status **/
    public static final int NAME_CONFLICT = 409;
    /** HTTP Header token that identifies ServiceUnAvailable status **/
    public static final int SERVICE_UNAVAILABLE = 503;
    /** HTTP Header token that identifies AuthFault status **/
    public static final int AUTH_FAULT = 500;

    /** HTTP Header token that identifies not implemented status **/
    public static final int NOT_IMPLMENTED = 501;

    public static final int DEFAULT_SERVER_FAULT = 500;

    /** Constants used for performing queries against the identity service **/
    public static final String CLOUD_TYPE = "cloud";
    public static final String NAST_TYPE = "nast";
    public static final String MOSSO_TYPE = "mosso";
    public static final String TOKEN_PATH = "tokens";
    public static final String GROUPS_PATH = "groups";
    public static final String ENDPOINTS_PATH = "endpoints";
    public static final String TENANT_PATH = "tenants";
    public static final String ROLES_PATH = "roles";
    public static final String PASSWORD = "password";
    public static final String DEFAULT_REGION = "defaultRegion";
    public static final String USER_PATH = "users";
    public static final String BASE_URL_PATH = "baseURLs";
    public static final String BASE_URL_REF_PATH = "baseURLRefs";
    public static final String AUTH_PATH = "auth";
    public static final String KEY_PATH = "key";
    public static final String SECRET_QA = "secretqa";

    public static final String KSDAM_PATH = "OS-KSADM";
    public static final String RAX_API_CRED = "RAX-KSKEY:apiKeyCredentials";
    public static final String RAX_GROUP = "RAX-GRPADM/groups";
    public static final String RAX_KSQA = "RAX-KSQA";
    public static final String RAX = "RAX-GRPADM";
    public static final String RAX_AUTH = "RAX-AUTH";
    public static final String IMPERSONATION_TOKENS_PATH = "impersonation-tokens";
    public static final String DOMAINS = "/domains";
    public static final String RATE_LIMIT_RTADM= "rtadm";

    /**Query params**/
    public static final String BELONGS_TO = "belongsTo";
    public static final String NAME = "name";
    public static final String TENANT_ID = "tenant_id";
    public static final String SERVICE_ID = "serviceId";
    public static final String MARKER = "marker";
    public static final String LIMIT = "limit";

    /** Headers **/
    public static final String X_TOKEN_HEADER = "X-Auth-Token";

    /**Namespaces **/
    public static final String RAX_KSADM_NS = "http://docs.openstack.org/identity/api/ext/OS-KSADM/v1.0";
    public static final String RAX_AUTH_NS = "http://docs.rackspace.com/identity/api/ext/RAX-AUTH/v1.0";
    public static final String RAX_GROUP_NS = "http://docs.rackspace.com/identity/api/ext/RAX-KSGRP/v1.0";

    /** Default error responses **/
    public static final String MISSING_PROP = "One or more values necessary for this request could not be found, please check the request and try again.";
    public static Properties MIMETYPES = new Properties ();

    static {
    	try
        {
    		MIMETYPES.load (IdentityConstants.class.getResourceAsStream("MIME.types"));
        }
        catch (IOException err)
        {
            logger.warn("Could not load MIME.types all refrences to IdentityConstants.MIMETYPES will return null.", err);
        }
    }

    /**
     * Convenience method to get a MIME Type.  If none is found it will return "application/octet-stream"
     *
     * @param fileExt
     * @return The suggested MIME type for the file extention.
     */
    public static String getMimetype (String fileExt)
    {
    	return IdentityConstants.MIMETYPES.getProperty(fileExt.toLowerCase(), "application/octet-stream");
    }

}
