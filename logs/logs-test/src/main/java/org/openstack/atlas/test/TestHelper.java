package org.openstack.atlas.test;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class TestHelper {
    public static final String NODEIP = "172.17.2.193:80";

    private TestHelper() {

    }

    // DO NOT CHANGE THIS LOGLINE GENERATOR. TESTS DEPEND ON THIS!
    public static String generateDynamicLogLine(String fqdn, String time) {
        return fqdn
                + " 84.77.249.117 - - ["
                + time
                + " -500] \"GET index.php?foo=bar HTTP/1.1\" 200 305 \"www.referrer.com\" "
                + "\"Mozilla/5.0 (somefoo)\" RT=0.287785435709213 ET=0.573905952899099 NODE=" + NODEIP + "\n";
    }

    // DO NOT CHANGE THIS LOGLINE GENERATOR. TESTS DEPEND ON THIS!
    public static String generateDynamicLogLine(String fqdn, String time, String nodeIp) {
        return fqdn
                + " 84.77.249.117 - - ["
                + time
                + " -500] \"GET index.php?foo=bar HTTP/1.1\" 200 305 \"www.referrer.com\" "
                + "\"Mozilla/5.0 (somefoo)\" RT=0.287785435709213 ET=0.573905952899099 NODE=" + nodeIp + "\n";
    }

    // DO NOT CHANGE THIS LOGLINE GENERATOR. TESTS DEPEND ON THIS!
    public static String generateLogLine(String fqdn, String time) {
        return fqdn
                + " 84.77.249.117 - - ["
                + time
                + " -500] \"GET index.php HTTP/1.1\" 200 305 \"www.referrer.com\" "
                + "\"Mozilla/5.0 (somefoo)\" RT=0.287785435709213 ET=0.573905952899099 NODE=" + NODEIP + "\n";
    }

    // DO NOT CHANGE THIS LOGLINE GENERATOR. TESTS DEPEND ON THIS!
    public static String generateLogLine(String fqdn, String time, String nodeIp) {
        return fqdn
                + " 84.77.249.117 - - ["
                + time
                + " -500] \"GET index.php HTTP/1.1\" 200 305 \"www.referrer.com\" "
                + "\"Mozilla/5.0 (somefoo)\" RT=0.287785435709213 ET=0.573905952899099 NODE=" + nodeIp + "\n";
    }

    public static String sanitizeCurrentDir(String currentdir) {
        int lastIndexOfSlash = currentdir.lastIndexOf("/");
        if (currentdir.substring(lastIndexOfSlash + 1).startsWith("stats-")) {
            // assume its in a child project of the main project, chop it off
            currentdir = currentdir.substring(0, lastIndexOfSlash + 1);
        }
        return currentdir;

    }

    public static Map getLines(Class clazz) throws Exception {
        String filename = "/statslines.txt";
        URL resource = TestHelper.class.getResource(filename);
        sun.net.www.content.text.PlainTextInputStream bis = (sun.net.www.content.text.PlainTextInputStream) resource
                .getContent();
        StringBuffer str = new StringBuffer();
        boolean inComment = false;
        while (bis.available() > 0) {
            char c = (char) bis.read();
            // start of comment line
            if (c == '#') {
                inComment = true;
            }

            if (!inComment) {
                str.append(c);
            }

            // comment line is done.
            if (inComment && c == '\n') {
                inComment = false;
            }
        }
        bis.close();

        String[] lines = str.toString().split("\n");
        if (lines.length % 2 != 0) {
            throw new RuntimeException(
                    "Rows in statslines.txt do not add up. cannot formulate a logContents and logline "
                            + "(must be an even number, read comment in file).");
        }

        Map items = new HashMap();
        for (int i = 0; i < lines.length / 2; i++) {
            String contentsLine = lines[i * 2 + 1];
            String[] contents = contentsLine.split(",");

            Constructor struct = clazz.getConstructor(String.class, Long.class, Float.class, String.class,
                    String.class, String.class, Boolean.class, String.class, String.class, String.class,
                    String.class, String.class);

            items.put(lines[i * 2], struct.newInstance(contents[0], Long.valueOf(contents[1]),
                    Float.valueOf(contents[2]), contents[3], contents[4], contents[5],
                    Boolean.valueOf(contents[6]), contents[7], contents[8], contents[9],
                    contents[10], contents[11]));
        }
        return items;
    }
}
