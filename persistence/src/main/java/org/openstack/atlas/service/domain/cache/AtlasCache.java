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
    private static MemcachedClient cacheClient;
    private String ttl = "300";

    @Autowired
    public AtlasCache(RestApiConfiguration configuration) throws IOException {
        String cacheHosts;
        //need to get config from a common place..
        if (configuration.hasKeys(ServicesConfigurationKeys.memcached_servers)) {
            cacheHosts = configuration.getString(ServicesConfigurationKeys.memcached_servers);
            buildCacheInstance(cacheHosts);
        } else {
            throw new MissingFieldException("The cache server host(s) information could not be found for the current configuration.");
        }
        if (configuration.hasKeys(ServicesConfigurationKeys.ttl)) {
            this.ttl = configuration.getString(ServicesConfigurationKeys.ttl);
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
            o = cacheClient.get(key);
            if (o == null) {
                //Sometimes connection is stale
                LOG.info("Cache miss Retrying...");
                o = cacheClient.get(key);
            }
        } catch (RuntimeException ex) {
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
        cacheClient = new MemcachedClient(
                new BinaryConnectionFactory(),
                AddrUtil.getAddresses(cacheHosts));
    }
}
