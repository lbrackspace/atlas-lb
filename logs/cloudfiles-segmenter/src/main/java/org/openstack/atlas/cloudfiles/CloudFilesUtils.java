package org.openstack.atlas.cloudfiles;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.cloudfiles.objs.RequestObjects;
import org.openstack.atlas.util.common.VerboseLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.core.MediaType;
import org.openstack.atlas.cloudfiles.objs.AuthToken;
import org.openstack.atlas.cloudfiles.objs.FilesContainerList;
import org.openstack.atlas.cloudfiles.objs.FilesObjectList;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
import org.openstack.atlas.cloudfiles.objs.ResponseObjects;
import org.openstack.atlas.util.debug.Debug;

public class CloudFilesUtils {

    private final VerboseLogger vlog = new VerboseLogger(CloudFilesUtils.class, VerboseLogger.LogLevel.INFO);
    private static final Log LOG = LogFactory.getLog(CloudFilesUtils.class);
    private static final String AUTH_URI = "/v2.0/tokens";
    private CloudFilesConfig cfg;
    private String token;

    public CloudFilesUtils() {
    }

    public CloudFilesUtils(CloudFilesConfig cfg) {
        this.cfg = new CloudFilesConfig(cfg);
    }

    public ResponseContainer<FilesContainerList> listContainers() throws ParseException {
        ResponseContainer<FilesContainerList> resp;
        String uri = String.format("/v1/%s", cfg.getFilesAccount());
        Client client = new Client();
        resp = ResponseContainer.newResponseContainer(client.resource(cfg.getFilesEndpoint()).path(uri).accept("application/json").type("application/json").header("x-auth-token", token).get(ClientResponse.class));
        String rawEntity = resp.getRawEntity();
        FilesContainerList fcl = ResponseObjects.decodeFilesContainersList(rawEntity);
        resp.setEntity(fcl);
        return resp;
    }

    public ResponseContainer<FilesObjectList> listContainer(String container) throws ParseException {
        ResponseContainer<FilesObjectList> resp = new ResponseContainer<FilesObjectList>();
        String uri = String.format("/v1/%s/%s", cfg.getFilesAccount(), container);
        Client client = new Client();
        resp = ResponseContainer.newResponseContainer(client.resource(cfg.getFilesEndpoint()).path(uri).accept("application/json").type("application/json").header("x-auth-token", token).get(ClientResponse.class));
        String rawEntity = resp.getRawEntity();
        if (resp.getClientResponse().getStatus() == 200) {
            FilesObjectList fol = ResponseObjects.decodeFilesObjectList(rawEntity);
            resp.setEntity(fol);
        }
        return resp;
    }

    public ResponseContainer<AuthToken> getAuthToken() throws ParseException {
        String user = cfg.getUser();
        String key = cfg.getApiKey();
        String authEndpoint = cfg.getAuthEndpoint();
        Client client = new Client();
        String reqJson = RequestObjects.authRequest(user, key);
        WebResource wr = client.resource(authEndpoint).path(AUTH_URI);
        ResponseContainer<AuthToken> resp = ResponseContainer.newResponseContainer(wr.accept("application/json").type("application/json").post(ClientResponse.class, reqJson));
        if (resp.getClientResponse().getStatus() == 200) {
            String jsonBody = resp.getRawEntity();
            AuthToken authToken = ResponseObjects.decodeAuthToken(jsonBody);
            resp.setEntity(authToken);
            token = authToken.getToken();
        }
        return resp;
    }

    public CloudFilesConfig getCfg() {
        return cfg;
    }

    public void setCfg(CloudFilesConfig cfg) {
        this.cfg = cfg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
