package org.openstack.atlas.api.helpers;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.exceptions.MissingFieldException;
import org.openstack.atlas.cfg.Configuration;

import java.io.IOException;

public class AtlasCache {
    final Log LOG = LogFactory.getLog(AtlasCache.class);
    private static MemcachedClient cacheClient;
    private static final int ttl = 300;

    private AtlasCache(Configuration configuration) throws IOException {
        String cacheHosts;
        if (configuration.hasKeys(PublicApiServiceConfigurationKeys.memcached_servers)) {
            cacheHosts = configuration.getString(PublicApiServiceConfigurationKeys.memcached_servers);
            buildCacheInstance(cacheHosts);
        } else {
            throw new MissingFieldException("The cache server host(s) information could not be found for the current configuration.");
        }
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
        set(key, ttl, o);
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
            o = cacheClient.get(key);
            if (o == null) {
                LOG.info("Cache MISS for KEY: " + key + "Retrying...");
                o = cacheClient.get(key);
            }
        } catch (RuntimeException ex) {
            //Server is down...
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
            //Server is down...
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
            //Server is down...
        }
        return false;
    }

    /**
     * This method builds cache client
     *
     * @param cacheHosts the hosts names to connect to
     * @throws IOException
     */
    private void buildCacheInstance(String cacheHosts) throws IOException {
        cacheClient = new MemcachedClient(
                new BinaryConnectionFactory(),
                AddrUtil.getAddresses(cacheHosts));
    }
}
