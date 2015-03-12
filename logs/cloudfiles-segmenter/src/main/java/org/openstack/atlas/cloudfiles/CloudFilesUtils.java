package org.openstack.atlas.cloudfiles;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import org.openstack.atlas.util.common.CloudFilesSegment;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.cloudfiles.objs.RequestObjects;
import org.openstack.atlas.util.common.VerboseLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cloudfiles.objs.AuthToken;
import org.openstack.atlas.cloudfiles.objs.FilesContainerList;
import org.openstack.atlas.cloudfiles.objs.FilesObject;
import org.openstack.atlas.cloudfiles.objs.FilesObjectList;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
import org.openstack.atlas.cloudfiles.objs.ResponseObjects;
import org.openstack.atlas.util.debug.Debug;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.openstack.atlas.util.common.SegmentedInputStream;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstack.atlas.cloudfiles.objs.comparators.FilesObjectComparator;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.debug.SillyTimer;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class CloudFilesUtils {

    public static final Pattern hdfsLzoPatternPre = Pattern.compile("^([0-9]{10})-access_log.aggregated.lzo$");
    protected static HdfsUtils hu;
    protected final VerboseLogger vlog = new VerboseLogger(CloudFilesUtils.class, VerboseLogger.LogLevel.INFO);
    protected static final Pattern fragPattern = Pattern.compile("^frag\\.([0-9]+)$");
    protected static final boolean DEBUG = false;
    protected static final String MD5 = "MD5";
    protected static final int BUFFSIZE = 64 * 1024;
    protected static final int CHUNK_SIZE = 64 * 1024;
    protected static final Log LOG = LogFactory.getLog(CloudFilesUtils.class);
    protected static final String AUTH_URI = "/v2.0/tokens";
    protected static final String JSON = "application/json";
    protected static final String BINARY = "application/octet-stream";
    protected static final String UTF8 = "UTF-8";
    protected static final String XAUTH = "x-auth-token";
    protected static final String ETAG = "ETag";
    protected static final FilesObjectComparator foComparator = new FilesObjectComparator();
    protected static SecureRandom rnd;
    protected static final CloudFilesUtils inst;
    protected String token;

    static {
        rnd = new SecureRandom();
        inst = new CloudFilesUtils();
    }

    public static CloudFilesUtils getInstance() {
        return inst;
    }

    public CloudFilesUtils() {
    }

    public ResponseContainer<AuthToken> getAuthToken() throws ParseException {
        String user = CloudFilesConfig.getUser();
        String key = CloudFilesConfig.getApiKey();
        String authEndpoint = CloudFilesConfig.getAuthEndpoint();
        Client client = new Client();
        String reqJson = RequestObjects.authRequest(user, key);
        Builder wb = client.resource(authEndpoint).path(AUTH_URI).accept(JSON).type(JSON);
        ResponseContainer<AuthToken> resp = ResponseContainer.newResponseContainer(wb.post(ClientResponse.class, reqJson));
        if (resp.getClientResponse().getStatus() == 200) {
            String jsonBody = resp.getRawEntity();
            AuthToken authToken = ResponseObjects.decodeAuthToken(jsonBody);
            resp.setEntity(authToken);
            setToken(authToken.getToken());
        }
        return resp;
    }

    public List<ResponseContainer<Boolean>> writeSegmentContainer(String containerName, CloudFilesSegmentContainer segContainer) {
        List<ResponseContainer<Boolean>> respList = new ArrayList<ResponseContainer<Boolean>>();

        List<WriteObjectThread> threads = new ArrayList<WriteObjectThread>();
        for (CloudFilesSegment seg : segContainer.getSegments()) {
            WriteObjectThread thread = new WriteObjectThread(this, containerName, seg);
            threads.add(thread);
            thread.start();
        }
        for (WriteObjectThread thread : threads) {
            ResponseContainer<Boolean> resp = new ResponseContainer<Boolean>();
            try {
                thread.join();
            } catch (InterruptedException ex) {
                resp.setRawEntity(Debug.getEST(ex));
                resp.setEntity(Boolean.FALSE);
                respList.add(resp);
                continue;
            }
            resp = thread.getResponse();
            respList.add(resp);
        }
        return respList;
    }

    public ResponseContainer<Boolean> writeObjectSegment(String container, CloudFilesSegment seg) {
        ResponseContainer<Boolean> resp = new ResponseContainer<Boolean>();
        String filePath = seg.getFileName();
        String md5Sum = seg.getMd5sum();
        if (md5Sum == null) {
            try {
                seg.computeMd5sum();
            } catch (Exception ex) {
                resp.setException(ex);
                return resp;
            }
        }
        long offset = seg.getOffset();
        int size = seg.getSize();
        int fragNumber = seg.getFragNumber();
        String fragStr = StaticStringUtils.lpad(Integer.toString(fragNumber), "0", 4);
        String objectName = String.format("frag.%s", fragStr);
        return writeObject(container, objectName, filePath, md5Sum, offset, size);
    }

    public ResponseContainer<Boolean> readObject(String container, String objectName, String filePath) throws IOException {
        FileOutputStream os = null;
        ResponseContainer<Boolean> resp = null;
        try {
            os = new FileOutputStream(new File(StaticFileUtils.expandUser(filePath)));
            resp = readObject(container, objectName, os);
        } finally {
            StaticFileUtils.close(os);
        }
        return resp;
    }

    public List<ResponseContainer<Boolean>> readSegmentContainer(String container, OutputStream os) throws ParseException, NoSuchAlgorithmException, IOException {
        List<ResponseContainer<Boolean>> respList = new ArrayList<ResponseContainer<Boolean>>();
        ResponseContainer<FilesObjectList> cntResponse = listContainer(container);
        Map<Integer, String> hashMap = new HashMap<Integer, String>();
        if (cntResponse.getEntity() == null) {
            throw new IOException("Error getting segment container", cntResponse.getException());
        }
        for (FilesObject fileObject : cntResponse.getEntity().getList()) {
            String fileName = fileObject.getName();
            int fragNum = regexFragNumber(fileName);
            if (fragNum < 0) {
                continue;
            }
            String md5 = fileObject.getHash();
            hashMap.put(fragNum, md5);
        }
        List<Integer> fragInts = new ArrayList<Integer>(hashMap.keySet());
        Collections.sort(fragInts);
        for (Integer fragInt : fragInts) {
            String fileName = String.format("frag.%s", StaticStringUtils.lpadLong((long) fragInt, "0", 4));
            String md5Sum = hashMap.get(fragInt);
            try {
                getAuthToken();
            } catch (Exception ex) {
                // Nothing to do.
            }
            ResponseContainer<Boolean> resp = readObject(container, fileName, os);
            if (!resp.getComment().equals(md5Sum)) {
                resp.setEntity(Boolean.FALSE);
            } else {
                resp.setEntity(Boolean.TRUE);
            }
            respList.add(resp);
        }
        return respList;
    }

    public static int regexFragNumber(String objectName) {
        Matcher m = fragPattern.matcher(objectName);
        if (m.find()) {
            int val = Integer.valueOf(m.group(1));
            return val;
        }
        return -1;
    }

    public ResponseContainer<Boolean> readObject(String container, String objectName, OutputStream os) {
        ResponseContainer<Boolean> resp = new ResponseContainer<Boolean>();
        resp.setComment("");
        resp.setComment(String.format("%s:%s", container, objectName));
        byte[] buff = new byte[BUFFSIZE];
        int nBytes = 0;
        InputStream is = null;
        HttpURLConnection con = null;
        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            String uri = String.format("%s/v1/%s/%s/%s",
                    CloudFilesConfig.getFilesEndpoint(),
                    CloudFilesConfig.getFilesAccount(),
                    escape(container),
                    escape(objectName));
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();
            if (con == null) {
                throw new IOException(String.format("Error connecting to %s", url));
            }
            con.setRequestMethod("GET");
            con.setDoOutput(false);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty(XAUTH, getToken());
            con.setRequestProperty("Content-Type", BINARY);
            con.setRequestProperty("Accept", BINARY);
            con.connect();
            is = con.getInputStream();
            while (true) {
                nBytes = is.read(buff, 0, BUFFSIZE);
                if (nBytes <= 0) {
                    break;
                }
                md.update(buff, 0, nBytes);
                os.write(buff, 0, nBytes);
            }
            int statusCode = con.getResponseCode();
            resp.setStatusCode(statusCode);
            if (statusCode == 200) {
                resp.setEntity(Boolean.TRUE);
                resp.setComment(BitConverters.bytes2hex(md.digest()));
            } else {
                resp.setEntity(Boolean.FALSE);
            }
        } catch (Exception ex) {
            resp.setException(ex);
        } finally {
            tryDisconnect(con);
            StaticFileUtils.close(is);
        }
        return resp;
    }

    public ResponseContainer<Boolean> writeObject(String container, String objectName, String filePath, String md5sum) {
        File file = new File(StaticFileUtils.expandUser(filePath));
        long size = file.length();
        return writeObject(container, objectName, filePath, md5sum, 0, (int) size);
    }

    public ResponseContainer<Boolean> writeObject(String container, String objectName, String filePath, String md5Sum, long offset, int length) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SillyTimer timer = new SillyTimer();
        timer.start();
        int nBytes;
        int nBytesRead;
        long nBytesLeft = length;
        RandomAccessFile ra = null;
        OutputStream os = null;
        HttpURLConnection con = null;
        ResponseContainer<Boolean> resp = new ResponseContainer();
        resp.setComment(String.format("%s:%s", container, objectName));
        try {
            String uri = String.format("%s/v1/%s/%s/%s", CloudFilesConfig.getFilesEndpoint(),
                    CloudFilesConfig.getFilesAccount(),
                    escape(container), escape(objectName));
            URL url = new URL(uri);
            byte[] buff = new byte[BUFFSIZE];
            if (md5Sum == null) {
                md5Sum = CloudFilesSegment.computeMd5SumForFile(filePath, offset, length, BUFFSIZE);
            }
            ra = StaticFileUtils.openRandomReadFile(filePath);
            ra.seek(offset);
            con = (HttpURLConnection) url.openConnection();
            if (con == null) {
                throw new IOException(String.format("Error connecting to %s", url));
            }
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty(XAUTH, getToken());
            con.setRequestProperty("Content-Type", BINARY);
            con.setRequestProperty("Accept", JSON);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setFixedLengthStreamingMode(length);
            con.setRequestProperty(ETAG, md5Sum);
            os = con.getOutputStream();
            timer.reset();
            while (nBytesLeft > 0) {
                nBytes = (nBytesLeft < BUFFSIZE) ? (int) nBytesLeft : BUFFSIZE;
                nBytesRead = ra.read(buff, 0, nBytes);
                os.write(buff, 0, nBytes);
                nBytesLeft -= nBytesRead;
                if (DEBUG) {
                    System.out.printf("wrote %d bytes: bytesLeft = %d\n", nBytes, nBytesLeft);
                }
            }
            ra.close();
            os.close();
            timer.stop();

            double seconds = timer.readSeconds();
            int statusCode = con.getResponseCode();
            resp.setStatusCode(statusCode);
            resp.setRawEntity(con.getResponseMessage());
            if (statusCode == 201 || statusCode == 202) {
                resp.setEntity(Boolean.TRUE);
            } else {
                resp.setEntity(Boolean.FALSE);
            }
        } catch (Exception ex) {
            resp.setException(ex);
            resp.setEntity(Boolean.FALSE);
        } finally {
            tryDisconnect(con);
            StaticFileUtils.close(ra);
            StaticFileUtils.close(os);
        }
        return resp;
    }

    public ResponseContainer<FilesContainerList> listContainers() throws ParseException {
        ResponseContainer<FilesContainerList> resp;
        String uri = String.format("/v1/%s", CloudFilesConfig.getFilesAccount());
        MultivaluedMap<String, String> params = newParams("dummy", rndIntStr());
        Client client = new Client();
        Builder wb = newFilesRequestBuilder(client, uri, params);
        resp = ResponseContainer.newResponseContainer(wb.get(ClientResponse.class));
        String rawEntity = resp.getRawEntity();
        FilesContainerList fcl = ResponseObjects.decodeFilesContainersList(rawEntity);
        resp.setEntity(fcl);
        return resp;
    }

    public ResponseContainer<Boolean> createContainer(String containerName) {
        ResponseContainer<Boolean> resp;
        String uri = String.format("/v1/%s/%s", CloudFilesConfig.getFilesAccount(), containerName);
        Client client = new Client();
        Builder wb = newFilesRequestBuilder(client, uri);
        resp = ResponseContainer.newResponseContainer(wb.put(ClientResponse.class));
        int statusCode = resp.getStatusCode();
        if (statusCode == 201 || statusCode == 202) {
            resp.setEntity(Boolean.TRUE);
        } else {
            resp.setEntity(Boolean.FALSE);
        }
        return resp;
    }

    public ResponseContainer<Boolean> deleteObject(String containerName, String objectName) {
        ResponseContainer<Boolean> resp;
        String uri = String.format("/v1/%s/%s/%s", CloudFilesConfig.getFilesAccount(), containerName, objectName);
        Client client = new Client();
        Builder wb = newFilesRequestBuilder(client, uri);
        resp = ResponseContainer.newResponseContainer(wb.delete(ClientResponse.class));
        if (resp.getStatusCode() == 204) {
            resp.setEntity(Boolean.TRUE);
        } else {
            resp.setEntity(Boolean.FALSE);
        }
        return resp;
    }

    public ResponseContainer<Boolean> deleteContainer(String container) {
        ResponseContainer<Boolean> resp;
        String uri = String.format("/v1/%s/%s", CloudFilesConfig.getFilesAccount(), container);
        Client client = new Client();
        Builder wb = newFilesRequestBuilder(client, uri);
        resp = ResponseContainer.newResponseContainer(wb.delete(ClientResponse.class));
        if (resp.getStatusCode() == 204) {
            resp.setEntity(Boolean.TRUE);
        } else {
            resp.setEntity(Boolean.FALSE);
        }
        return resp;
    }

    public ResponseContainer<Integer> emptyContainer(String container) throws ParseException {
        int nFilesDeleted = 0;
        ResponseContainer<FilesObjectList> objectsListResp = listContainer(container);
        ResponseContainer<Integer> resp = new ResponseContainer<Integer>();
        if (objectsListResp.getStatusCode() != 200) {
            resp.setStatusCode(-1);
            resp.setRawEntity(objectsListResp.getRawEntity());
            resp.setEntity(nFilesDeleted);
            return resp;
        }
        for (FilesObject fo : objectsListResp.getEntity().getList()) {
            String fileName = fo.getName();
            ResponseContainer<Boolean> deleteResp = deleteObject(container, fileName);
            if (deleteResp.getEntity()) {
                nFilesDeleted++;
            }
        }
        resp.setStatusCode(-1);
        resp.setEntity(nFilesDeleted);
        return resp;
    }

    public ResponseContainer<FilesObjectList> listContainer(String container) throws ParseException {
        ResponseContainer<FilesObjectList> resp = new ResponseContainer<FilesObjectList>();
        String uri = String.format("/v1/%s/%s", CloudFilesConfig.getFilesAccount(), container);
        Client client = new Client();
        Builder wb = newFilesRequestBuilder(client, uri);
        resp = ResponseContainer.newResponseContainer(wb.get(ClientResponse.class));
        String rawEntity = resp.getRawEntity();
        if (resp.getClientResponse().getStatus() == 200) {
            FilesObjectList fol = ResponseObjects.decodeFilesObjectList(rawEntity);
            Collections.sort(fol.getList(), foComparator);
            resp.setEntity(fol);
        }
        return resp;
    }

    public static String hourKeyToFileName(int hourKey) {
        return StaticStringUtils.lpadLong((long) hourKey, "0", 10)
                + "-access_log.aggregated.lzo";
    }

    public List<Integer> listLzoHourKeysFromLocal(String filePath) throws IOException {
        List<Integer> hourKeys = new ArrayList<Integer>();
        List<String> filePaths = StaticFileUtils.listDir(filePath);
        Matcher m = hdfsLzoPatternPre.matcher("");
        for (String foundFile : filePaths) {
            String baseName = StaticFileUtils.stripDirectoryFromFileName(foundFile);
            m.reset(baseName);
            if (m.find()) {
                String hourKeyStr = m.group(1);
                int hourKey = Integer.parseInt(hourKeyStr);
                hourKeys.add(hourKey);
            }

        }
        return hourKeys;
    }

    private Builder newFilesRequestBuilder(Client client, String uri, MultivaluedMap<String, String> params) {
        String ep = CloudFilesConfig.getFilesEndpoint();
        WebResource wr = client.resource(ep).path(uri);
        if (params != null) {
            wr = wr.queryParams(params);
        }
        Builder wb = wr.accept(JSON).type(JSON).header(XAUTH, getToken());
        return wb;
    }

    private Builder newFilesRequestBuilder(Client client, String uri) {
        return newFilesRequestBuilder(client, uri, null);
    }

    public String getToken() {
        String tokenOut;
        synchronized (CloudFilesUtils.class) {
            tokenOut = new String(token);
        }
        return tokenOut;
    }

    public void setToken(String token) {
        synchronized (CloudFilesUtils.class) {
            this.token = token;
        }
    }

    private String rndIntStr() {
        return String.format("%d", Math.abs(rnd.nextInt()));
    }

    private static MultivaluedMap<String, String> newParams(String... args) {
        int i;
        int n;
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("number of args must be even");
        }
        MultivaluedMap<String, String> mvMap = new MultivaluedMapImpl();
        n = args.length;
        for (i = 0; i < n; i += 2) {
            mvMap.add(args[i], args[i + 1]);
        }
        return mvMap;
    }

    private static String escape(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, UTF8);
    }

    private void tryDisconnect(HttpURLConnection con) {
        try {
            con.disconnect();
        } catch (Exception ex) {
            // Must already be closed
        }
    }
}
