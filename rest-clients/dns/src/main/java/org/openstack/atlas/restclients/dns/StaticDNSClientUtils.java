package org.openstack.atlas.restclients.dns;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map.Entry;
import com.sun.jersey.api.client.ClientResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.openstack.atlas.util.b64aes.Base64;

public class StaticDNSClientUtils {
    private static final int SB_INIT_SIZE = 1024 * 4;
    public static String clientResponseToString(ClientResponse resp) {
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        sb.append("ClientResponse: ");
        if (resp == null) {
            sb.append("null\n");
            return sb.toString();
        }
        sb.append("\nStatus code: ").append(Integer.valueOf(resp.getStatus()).toString()).append("\n");
        MultivaluedMap<String, String> headers = resp.getHeaders();
        if (headers == null) {
            sb.append("Headers: null\n");
        } else {
            sb.append("Headers: \n");
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                if(key == null){
                    key = "null";
                }
                for (String value : entry.getValue()) {
                    if(value==null){
                        value="null";
                    }
                    sb.append(key).append(":").append(value).append("\n");
                }
            }
        }
        String body = resp.getEntity(String.class);
        if(body == null){
            body = "null";
        }
        return sb.append("\n").append(body).append("\n").toString();
    }

    public static String encodeBasicAuth(String user,String passwd) throws UnsupportedEncodingException{
        if(user == null || passwd == null){
            throw new UnsupportedEncodingException("user or passwd must not be null");
        }
        return "BASIC " + Base64.encode(user + ":" + passwd);
    }
}
