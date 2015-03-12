package org.openstack.atlas.cloudfiles.objs;

import org.json.simple.JSONObject;
import org.json.simple.JSONObject;

public class RequestObjects {

    public static String authRequest(String userName, String apiKey) {
        JSONObject cred = new JSONObject();
        cred.put("username", userName);
        cred.put("apiKey", apiKey);
        JSONObject auth = new JSONObject();
        auth.put("RAX-KSKEY:apiKeyCredentials", cred);
        JSONObject req = new JSONObject();
        req.put("auth", auth);
        return req.toJSONString();
    }
}
