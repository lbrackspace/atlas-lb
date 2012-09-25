package org.openstack.atlas.service.domain.services.helpers.authmangler;

import com.sun.jersey.api.client.Client;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.KeyStoneException;

public class AuthAdminClient extends KeyStoneAdminClient{

    public AuthAdminClient(String authUrl, String basicAuthKey, String basicAuthUserName, Client client) throws KeyStoneException {
        super(authUrl,basicAuthKey,basicAuthUserName,client);
    }
     public AuthAdminClient(String authUrl, String basicAuthKey, String basicAuthUserName) throws KeyStoneException{
         super(authUrl,basicAuthKey,basicAuthUserName);
     }

     public AuthAdminClient(String authUrl) throws KeyStoneException{
         super(authUrl);
     }

     public AuthAdminClient() throws KeyStoneException {
     }

     public AuthAdminClient(KeyStoneConfig ksc) throws KeyStoneException{
         super(ksc.getAdminUrl(),ksc.getAdminKey(),ksc.getAdminUser());
     }

}
