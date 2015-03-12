package org.openstack.atlas.cloudfiles.objs;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.debug.Debug;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONArray;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class ResponseObjects {

    private static final VerboseLogger vlog = new VerboseLogger(ResponseObjects.class, VerboseLogger.LogLevel.INFO);

    public static FilesContainerList decodeFilesContainersList(String containerJson) throws ParseException {
        FilesContainerList filesList = new FilesContainerList();
        JSONParser jp = new JSONParser();
        JSONArray ja = (JSONArray) jp.parse(containerJson);
        int i;
        int n = ja.size();
        for (i = 0; i < n; i++) {
            JSONObject containerObj = (JSONObject) ja.get(i);
            int count = ((Long) containerObj.get("count")).intValue();
            long bytes = (Long) containerObj.get("bytes");
            String name = (String) containerObj.get("name");
            filesList.getContainers().add(new FilesContainer(name, count, bytes));
        }
        return filesList;
    }

    public static FilesObjectList decodeFilesObjectList(String objectListJson) throws ParseException {
        FilesObjectList fol = new FilesObjectList();
        JSONParser jp = new JSONParser();
        JSONArray ja = (JSONArray) jp.parse(objectListJson);
        int i;
        int n = ja.size();
        for (i = 0; i < n; i++) {
            JSONObject obj = (JSONObject) ja.get(i);
            long bytes = (Long) obj.get("bytes");
            String hash = (String) obj.get("hash");
            String name = (String) obj.get("name");
            String contentType = (String) obj.get("content_type");
            String isoDate = (String) obj.get("last_modified");
            DateTime lastModified = StaticDateTimeUtils.iso8601ToDateTime(isoDate);
            FilesObject fo = new FilesObject(bytes, lastModified, hash, name, contentType);
            fol.getObjectsList().add(fo);
        }
        return fol;
    }

    public static AuthToken decodeAuthToken(String authJsonResponse) throws ParseException {
        JSONParser jp = new JSONParser();
        JSONObject obj = (JSONObject) jp.parse(authJsonResponse);
        JSONObject access = (JSONObject) obj.get("access");
        JSONObject token = (JSONObject) access.get("token");

        String id = (String) token.get("id");
        String iso8601Dt = (String) token.get("expires");
        DateTime dt = StaticDateTimeUtils.iso8601ToDateTime(iso8601Dt);
        AuthToken at = new AuthToken();
        at.setExpires(dt);
        at.setToken(id);
        return at;
    }
}
