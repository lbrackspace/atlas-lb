package org.openstack.atlas.atom.util;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientParamBean;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParamBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import ru.hh.jersey.hchttpclient.ApacheHttpClient;
import ru.hh.jersey.hchttpclient.ApacheHttpClientHandler;

public class ClientUtil {

    /**
     * Takes no params, builds jersey client using jersey-hc and ApacheHttpClient
     *
     * @return the configured ApacheHttpClient
     */
    public static ApacheHttpClient makeHttpClient() {


        HttpParams params = new BasicHttpParams();
        HttpProtocolParamBean connParams = new HttpProtocolParamBean(params);
        connParams.setVersion(HttpVersion.HTTP_1_1);
        connParams.setUseExpectContinue(false);

        ConnManagerParamBean poolParams = new ConnManagerParamBean(params);
        poolParams.setMaxTotalConnections(2);
        poolParams.setTimeout(100);

        ClientParamBean clientParams = new ClientParamBean(params);
        clientParams.setMaxRedirects(10);
        clientParams.setAllowCircularRedirects(true);
        clientParams.setRejectRelativeRedirect(false);
        clientParams.setHandleAuthentication(false);

        // ------------------
        // continue as before

        SchemeRegistry schemata = new SchemeRegistry();
        schemata.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

//            ClientConnectionManager connManager = new SingleClientConnManager(params, schemata);
        ClientConnectionManager connManager = new ThreadSafeClientConnManager(params, schemata);
        HttpClient httpClient = new DefaultHttpClient(connManager, params);

        return new ApacheHttpClient(new ApacheHttpClientHandler(httpClient));
    }

}
