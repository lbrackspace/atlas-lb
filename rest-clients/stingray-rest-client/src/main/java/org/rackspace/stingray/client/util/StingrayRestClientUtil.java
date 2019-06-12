package org.rackspace.stingray.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.rackspace.stingray.client.pool.Pool;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class StingrayRestClientUtil {
    private static final Log LOG = LogFactory.getLog(StingrayRestClientUtil.class);

    public static class ClientHelper {

        public static Client configureClient(boolean isDebugging) {
            TrustManager[] certs = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }
                    }
            };
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, certs, new SecureRandom());
            } catch (java.security.GeneralSecurityException ex) {
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

            ClientConfig config = new ClientConfig();
            if (isDebugging) config.property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LoggingFeature.Verbosity.PAYLOAD_ANY);

            Client client = ClientBuilder.newBuilder().withConfig(config)
                    .sslContext(ctx).hostnameVerifier((hostname, session) -> true).build();
            return client;
        }

        public static Client createClient(boolean isDebugging) {
            return ClientHelper.configureClient(isDebugging);
        }


        public static List<Pool> parsePools(String poolsString) {
            List<Pool> pools = new ArrayList<Pool>();

            String[] parsedPools = poolsString.split("[\":{}\\[\\] ]+");
            for (String vals : parsedPools) {
                Pool pool = new Pool();
                pools.add(pool);
            }
            return new ArrayList<Pool>();
        }
    }
}
