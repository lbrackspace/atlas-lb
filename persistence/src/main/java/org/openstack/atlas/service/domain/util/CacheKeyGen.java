package org.openstack.atlas.service.domain.util;

public class CacheKeyGen {
    private final static String UID = "c21bf0a289fec5b7f7dfe62197570f94";
    private final static String API_PREFIX = "atlas-lb:";

    //Must be accountId and the object id to which the cached content references...
    public static String generateKeyName(Integer accountId, Integer... objectId) {
        int count = objectId.length;
        String key =  String.format("%s_%s", API_PREFIX + UID, accountId);
        for (int i=1; i<=count; i++) {
            key = String.format("%s_%s", key, objectId[i-1]);
        }
        return key;
    }

}