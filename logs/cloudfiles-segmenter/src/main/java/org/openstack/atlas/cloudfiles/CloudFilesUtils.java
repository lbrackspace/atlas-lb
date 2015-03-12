package org.openstack.atlas.cloudfiles;

import java.net.MalformedURLException;
import org.openstack.atlas.cloudfiles.objs.CloudFilesSegment;
import org.openstack.atlas.cloudfiles.objs.CloudFilesSegmentContainer;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.openstack.atlas.cloudfiles.objs.comparators.FilesObjectComparator;
import org.openstack.atlas.util.debug.SillyTimer;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class CloudFilesUtils {

    private final VerboseLogger vlog = new VerboseLogger(CloudFilesUtils.class, VerboseLogger.LogLevel.INFO);
    private static final int BUFFSIZE = 64 * 1024;
    private static final int CHUNK_SIZE = 64 * 1024;
    private static final Log LOG = LogFactory.getLog(CloudFilesUtils.class);
    private static final String AUTH_URI = "/v2.0/tokens";
    private static final String JSON = "application/json";
    private static final String BINARY = "application/octet-stream";
    private static final String UTF8 = "UTF-8";
    private static final String XAUTH = "x-auth-token";
    private static final String ETAG = "ETag";
    private static final FilesObjectComparator foComparator = new FilesObjectComparator();
    private static SecureRandom rnd;
    private CloudFilesConfig cfg;
    private String token;

    static {
        rnd = new SecureRandom();
    }

    public CloudFilesUtils() {
    }

    public CloudFilesUtils(CloudFilesConfig cfg) {
        this.cfg = new CloudFilesConfig(cfg);
    }

    public ResponseContainer<AuthToken> getAuthToken() throws ParseException {
        String user = cfg.getUser();
        String key = cfg.getApiKey();
        String authEndpoint = cfg.getAuthEndpoint();
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
            thread.start();
            threads.add(thread);
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

    public ResponseContainer<Boolean> writeObjectSegment(String container, CloudFilesSegment seg) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        ResponseContainer<Boolean> resp = new ResponseContainer<Boolean>();
        String filePath = seg.getFileName();
        String md5Sum = seg.getMd5sum();
        if (md5Sum == null) {
            seg.computeMd5sum();
        }
        long offset = seg.getOffset();
        int size = seg.getSize();
        int fragNumber = seg.getFragNumber();
        String fragStr = StaticStringUtils.lpad(Integer.toString(fragNumber), "0", 4);
        String objectName = String.format("frag.%s", fragStr);
        return writeObject(container, objectName, filePath, md5Sum, offset, size);
    }

    public ResponseContainer<Boolean> readObject(String container, String objectName, String filePath) throws FileNotFoundException, UnsupportedEncodingException, MalformedURLException, IOException {
        FileOutputStream os = new FileOutputStream(new File(StaticFileUtils.expandUser(filePath)));
        ResponseContainer<Boolean> resp = readObject(container, objectName, os);
        os.close();
        return resp;
    }

    public ResponseContainer<Boolean> readObject(String container, String objectName, OutputStream os) throws FileNotFoundException, UnsupportedEncodingException, MalformedURLException, IOException {
        ResponseContainer<Boolean> resp = new ResponseContainer<Boolean>();
        byte[] buff = new byte[BUFFSIZE];
        int nBytes = 0;
        String uri = String.format("%s/v1/%s/%s/%s", cfg.getFilesEndpoint(), cfg.getFilesAccount(), escape(container), escape(objectName));
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
        InputStream is = con.getInputStream();
        while (true) {
            nBytes = is.read(buff, 0, BUFFSIZE);
            if (nBytes < 0) {
                break;
            }
            os.write(buff, 0, nBytes);
        }
        int statusCode = con.getResponseCode();
        resp.setStatusCode(statusCode);
        if (statusCode == 200) {
            resp.setEntity(Boolean.TRUE);
        } else {
            resp.setEntity(Boolean.FALSE);
        }

        return resp;
    }

    public ResponseContainer<Boolean> writeObject(String container, String objectName, String filePath, String md5sum) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        File file = new File(StaticFileUtils.expandUser(filePath));
        long size = file.length();
        return writeObject(container, objectName, filePath, md5sum, 0, (int) size);
    }

    public ResponseContainer<Boolean> writeObject(String container, String objectName, String filePath, String md5Sum, long offset, int length) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SillyTimer timer = new SillyTimer();
        timer.start();
        int nBytes;
        int nBytesRead;
        long nBytesLeft = length;
        ResponseContainer resp = new ResponseContainer();
        String uri = String.format("%s/v1/%s/%s/%s", cfg.getFilesEndpoint(), cfg.getFilesAccount(), escape(container), escape(objectName));
        URL url = new URL(uri);
        byte[] buff = new byte[BUFFSIZE];
        if (md5Sum == null) {
            md5Sum = StaticFileUtils.computeMd5SumForFile(filePath, offset, length, BUFFSIZE);
        }
        RandomAccessFile ra = StaticFileUtils.openRandomReadFile(filePath);
        ra.seek(offset);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
        con.setFixedLengthStreamingMode(length);
        //con.setChunkedStreamingMode(CHUNK_SIZE);
        con.setRequestProperty(ETAG, md5Sum);
        OutputStream os = con.getOutputStream();
        timer.reset();
        while (nBytesLeft > 0) {
            nBytes = (nBytesLeft < BUFFSIZE) ? (int) nBytesLeft : BUFFSIZE;
            nBytesRead = ra.read(buff, 0, nBytes);
            os.write(buff, 0, nBytes);
            nBytesLeft -= nBytesRead;
            //System.out.printf("wrote %d bytes: bytesLeft = %d\n", nBytes, nBytesLeft);
        }
        ra.close();
        os.close();
        timer.stop();

        double seconds = timer.readSeconds();
        //System.out.printf("wrote %d bytes in %f seconds\n", length, seconds);
        //System.out.printf("rate = %f bytes per second\n", length / seconds);
        int statusCode = con.getResponseCode();
        resp.setStatusCode(statusCode);
        resp.setRawEntity(con.getResponseMessage());
        if (statusCode == 201 || statusCode == 202) {
            resp.setEntity(Boolean.TRUE);
        } else {
            resp.setEntity(Boolean.FALSE);
        }
        con.disconnect();
        return resp;
    }

    public ResponseContainer<FilesContainerList> listContainers() throws ParseException {
        ResponseContainer<FilesContainerList> resp;
        String uri = String.format("/v1/%s", cfg.getFilesAccount());
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
        String uri = String.format("/v1/%s/%s", cfg.getFilesAccount(), containerName);
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
        String uri = String.format("/v1/%s/%s/%s", cfg.getFilesAccount(), containerName, objectName);
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
        String uri = String.format("/v1/%s/%s", cfg.getFilesAccount(), container);
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
        String uri = String.format("/v1/%s/%s", cfg.getFilesAccount(), container);
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

    private Builder newFilesRequestBuilder(Client client, String uri, MultivaluedMap<String, String> params) {
        String ep = cfg.getFilesEndpoint();
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

    public CloudFilesConfig getCfg() {
        return cfg;
    }

    public void setCfg(CloudFilesConfig cfg) {
        this.cfg = cfg;
    }

    public synchronized String getToken() {
        return token;
    }

    public synchronized void setToken(String token) {
        this.token = token;
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
}
