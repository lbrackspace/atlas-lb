package org.openstack.atlas.util.itest.hibernate;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openstack.atlas.util.staticutils.StaticFileUtils;


public class HibernateStaticUtils {

    public static String readKeyFromJsonFile(String fileName) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException, org.json.simple.parser.ParseException {
        String jsonStr = new String(StaticFileUtils.readFile(new File(StaticFileUtils.expandUser(fileName))), "utf-8");
        JSONParser jp = new JSONParser();
        JSONObject jsonConf = (JSONObject) jp.parse(jsonStr);
        return (String) jsonConf.get("key");
    }

}
