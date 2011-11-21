package org.openstack.atlas.api.helpers;

public class CacheKeyNameGenerator {
    private final static String API_PREFIX = "atlas-lb";

    //Must be accountId and the object id to which the cached content references...
    public static String generateKeyName(Integer accountId, Integer objectId) {
        return String.format("%s_%s_%s", API_PREFIX, accountId, objectId);

    }
}
