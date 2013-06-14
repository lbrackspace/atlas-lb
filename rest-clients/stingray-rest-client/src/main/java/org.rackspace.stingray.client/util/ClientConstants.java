package org.rackspace.stingray.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class ClientConstants {
    private static final Log logger = LogFactory.getLog(ClientConstants.class);

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
    public static final String ACTIONSCRIPT_PATH = "actionprogs/";
    public static final String BANDWIDTH_PATH = "bandwidth/";
    public static final String EXTRAFILE_PATH = "extra/";
    public static final String GLB_PATH = "services/";
    public static final String LOCATION_PATH = "locations/";
    public static final String MONITOR_PATH = "monitors/";
    public static final String MONITORSCRIPT_PATH = "scripts/";
    public static final String PERSISTENCE_PATH = "persistence/";
    public static final String POOL_PATH = "pools/";
    public static final String PROTECTION_PATH = "protection/";
    public static final String RATE_PATH = "rate/";
    public static final String CACRL_PATH = "ssl/cas/";
    public static final String CLIENTKEYPAIR_PATH = "ssl/client_keys/";
    public static final String KEYPAIR_PATH = "ssl/server_keys/";
    public static final String TRAFFICMANAGER_PATH = "zxtms/";
    public static final String IP_PATH = "flipper/";
    public static final String TRAFFICSCRIPT_PATH = "rules/";
    public static final String V_SERVER_PATH = "vservers/";

    /** Error messages for the Stingray Rest Client**/
    public static final String REQUEST_ERROR = "The Stingray Rest Client encountered a problem processing the request: ";

    public static final String CLIENT_ERROR = "There was an error communicating with the auth service...";


    /** Default error responses **/
    public static final String MISSING_PROP = "One or more values necessary for this request could not be found, please check the request and try again.";
    public static Properties MIMETYPES = new Properties ();

    static {
    	try
        {
    		MIMETYPES.load (ClientConstants.class.getResourceAsStream("MIME.types"));
        }
        catch (IOException err)
        {
            logger.warn("Could not load MIME.types all refrences to IdentityConstants.MIMETYPES will return null.", err);
        }
    }

    /**
     * Convenience method to get a MIME Type.  If none is found it will return "application/octet-stream"
     *
     * @param fileExt   Provides the file extension
     * @return The suggested MIME type for the file extention.
     */
    public static String getMimetype (String fileExt)
    {
    	return ClientConstants.MIMETYPES.getProperty(fileExt.toLowerCase(), "application/octet-stream");
    }

}
