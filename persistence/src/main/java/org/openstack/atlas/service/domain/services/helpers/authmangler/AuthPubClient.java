package org.openstack.atlas.service.domain.services.helpers.authmangler;

import com.sun.jersey.api.client.Client;
import java.net.URISyntaxException;
import org.openstack.client.keystone.KeyStoneClient;
import org.openstack.client.keystone.KeyStoneException;
import org.openstack.client.keystone.auth.AuthData;

public class AuthPubClient extends KeyStoneClient {

    public AuthPubClient(String authUrl, Client client) throws KeyStoneException {
        super(authUrl,client);
    }

    public AuthPubClient(String authUrl) throws KeyStoneException {
        super(authUrl);
    }

    public AuthPubClient() throws KeyStoneException {
        super();
    }

    public AuthPubClient(KeyStoneConfig ksc) throws KeyStoneException{
        super(ksc.getPublicUrl());
    }

    public AuthData getToken(String user,String key) throws KeyStoneException, URISyntaxException{
        return authenticateUser(user, key);
    }
}
