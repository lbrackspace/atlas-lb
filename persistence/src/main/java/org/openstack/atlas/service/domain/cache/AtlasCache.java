package org.openstack.atlas.service.domain.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.cfg.ServicesConfigurationKeys;
import org.openstack.atlas.service.domain.exceptions.MissingFieldException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AtlasCache {
    private final Log LOG = LogFactory.getLog(AtlasCache.class);

    private RestApiConfiguration configuration;
    private MemcachedClient cacheClient;
    private String ttl = "300";
    private String cacheHosts;

    @Autowired
    public AtlasCache(RestApiConfiguration configuration) throws IOException {
        this.configuration = configuration;
        loadClient();
    }

    /**
     * This method will set the object in cache using the provided key and ttl
     *
     * @param key the key for the caching store object
     * @param ttl the ttl for the caching store object
     * @param o   the object to set
     */
    public void set(String key, int ttl, final Object o) {
        try {
            cacheClient.set(key, ttl, o);
        } catch (RuntimeException ex) {
            //Server is down..
        }
    }

    /**
     * This method will set the object in cache using the default ttl
     *
     * @param key the key for the caching store object
     * @param o   the object to set
     */
    public void set(String key, final Object o) {
        set(key, Integer.valueOf(this.ttl), o);
    }

    /**
     * This method will retrieve the object based on the provided key
     *
     * @param key the key for the caching store object
     * @return return the object
     */
    public Object get(String key) {

        Object o = null;
        try {
            checkState();

            o = cacheClient.get(key);
            if (o == null) {
                //Sometimes connection is stale
                LOG.info("Cache miss Retrying...");
                o = cacheClient.get(key);
            }
        } catch (Exception ex) {
            LOG.info("Server is down...");
        }
        return o;
    }

    /**
     * This method will delete the object using the provided key
     *
     * @param key the key for the caching store object
     * @return Future<Boolean>
     */
    public Object delete(String key) {
        try {
            return cacheClient.delete(key);
        } catch (RuntimeException ex) {
            LOG.info("Server is down...");
        }
        return null;
    }

    /**
     * This method will verify the key lives in the caching store
     *
     * @param key the key for the caching store object
     * @return boolean if the key belongs or not
     */
    public boolean containsKey(String key) {
        try {
            return cacheClient.get(key) != null;
        } catch (RuntimeException ex) {
            LOG.info("Server is down...");
        }
        return false;
    }

    /**
     * This method builds cache client
     *
     * @param cacheHosts the hosts names to connect to
     * @throws java.io.IOException
     */
    private void buildCacheInstance(String cacheHosts) throws IOException {
        LOG.info("Building memcached client...");
        cacheClient = new MemcachedClient(
                new BinaryConnectionFactory(),
                AddrUtil.getAddresses(cacheHosts));
    }

    /**
     * @throws IOException
     */
    private void loadClient() throws IOException {
        LOG.info("Loading memcached client configurations");

        //need to get config from a common place..
        if (configuration.hasKeys(ServicesConfigurationKeys.memcached_servers)) {
            this.cacheHosts = configuration.getString(ServicesConfigurationKeys.memcached_servers);
            buildCacheInstance(cacheHosts);
        } else {
            throw new MissingFieldException("The cache server host(s) information " +
                    "could not be found for the current configuration.");
        }
        if (configuration.hasKeys(ServicesConfigurationKeys.ttl)) {
            this.ttl = configuration.getString(ServicesConfigurationKeys.ttl);
        }
        LOG.info(String.format(
                "Loaded memcached client with configurations: memcached_servers: %s, ttl: %s", cacheHosts, ttl));
    }

    /**
     * @throws IOException
     */
    private void rebuildClient() throws IOException {
        LOG.info("Shutting down memcached client...");
        cacheClient.shutdown();
        loadClient();
    }

    /**
     * @throws IOException
     */
    private void checkState() throws IOException {
        LOG.info("Checking for modified configurations...");
        if (!configuration.getString(ServicesConfigurationKeys.memcached_servers).equals(cacheHosts)
                || !configuration.getString(ServicesConfigurationKeys.ttl).equals(this.ttl)) {
            LOG.info("Detected modification, reload memcached client");
            rebuildClient();
        }
    }
}
