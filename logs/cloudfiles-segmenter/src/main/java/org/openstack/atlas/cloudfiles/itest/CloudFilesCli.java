package org.openstack.atlas.cloudfiles.itest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.cloudfiles.CloudFilesConfig;
import org.openstack.atlas.cloudfiles.objs.CloudFilesSegmentContainer;
import org.openstack.atlas.cloudfiles.CloudFilesUtils;
import org.openstack.atlas.cloudfiles.objs.AuthToken;
import org.openstack.atlas.cloudfiles.objs.CloudFilesSegment;
import org.openstack.atlas.cloudfiles.objs.FilesContainer;
import org.openstack.atlas.cloudfiles.objs.FilesContainerList;
import org.openstack.atlas.cloudfiles.objs.FilesObject;
import org.openstack.atlas.cloudfiles.objs.FilesObjectList;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.debug.SillyTimer;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class CloudFilesCli {

    private BufferedReader stdin;
    private CloudFilesConfig cfg;
    private CloudFilesUtils cfu;
    private SillyTimer timer = new SillyTimer();
    private int segmentSize = 1024 * 1024;
    private static final int BUFFSIZE = 64 * 1024;

    public void run(String[] argv) throws FileNotFoundException, UnsupportedEncodingException, IOException, ParseException {
        cfg = CloudFilesConfig.readJsonConfig("~/cloudFiles.json");
        cfu = new CloudFilesUtils(cfg);
        stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);

        while (true) {
            try {
                System.out.printf("cloudFilesCli> ");
                System.out.flush();
                String cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break;// Eof
                }
                String[] args = StaticStringUtils.stripBlankArgs(cmdLine);
                Map<String, String> kwArgs = StaticStringUtils.argMapper(args);
                args = StaticStringUtils.stripKwArgs(args);
                if (args.length < 1) {
                    System.out.printf("usage is  help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("segmentsize <size>          #set the segment size\n");
                    System.out.printf("ls                          #list ContainerNames\n");
                    System.out.printf("lc <cnt>                    #list container\n");
                    System.out.printf("rmc <cnt>                   #Delete container\n");
                    System.out.printf("auth                        #get auth token\n");
                    System.out.printf("mkcnt <cnt>                 #create container\n");
                    System.out.printf("segup <cnt> <filePath>      #segment the files and upload\n");
                    System.out.printf("seg <filePah>               #segmentfile and view\n");
                    System.out.printf("up <cnt> <obj> <filePath>   #upload single file\n");
                    System.out.printf("down <cnt> <obj> <filePath> #download single file\n");
                    System.out.printf("bulk <fileName> <size>      #write junk bytes to file for testing\n");

                } else if (cmd.equals("bulk") && args.length >= 3) {
                    int i;
                    String fileName = args[1];
                    long size = Long.valueOf(args[2]);
                    long nBytesLeft = size;
                    int nBytes;
                    byte[] buff = new byte[BUFFSIZE];
                    for (i = 0; i < BUFFSIZE; i++) {
                        buff[i] = 10;
                    }
                    FileOutputStream os = new FileOutputStream(new File(StaticFileUtils.expandUser(fileName)));
                    i = 0;
                    while (nBytesLeft > 0) {
                        if ((i & 15) == 0) {
                            System.out.printf(".");
                            System.out.flush();
                        }
                        nBytes = (nBytesLeft < BUFFSIZE) ? (int) nBytesLeft : BUFFSIZE;
                        os.write(buff, 0, nBytes);
                        nBytesLeft -= nBytes;
                        i++;
                    }
                    System.out.printf("\n");
                } else if (cmd.equals("down") && args.length >= 4) {
                    String containerName = args[1];
                    String objectName = args[2];
                    String filePath = args[3];
                    System.out.printf("Downloading container %s object %s to file %s\n", containerName, objectName, filePath);
                    ResponseContainer<Boolean> resp = cfu.readObject(containerName, objectName, filePath);
                    System.out.printf("%d: %s\n", resp.getStatusCode(), resp.getEntity());
                } else if (cmd.equals("up") && args.length >= 4) {
                    String containerName = args[1];
                    String objectName = args[2];
                    String filePath = args[3];
                    System.out.printf("Writing file %s to container %s as object %s\n", filePath, containerName, objectName);
                    ResponseContainer<Boolean> resp = cfu.writeObject(containerName, objectName, filePath, null);
                    System.out.printf("%d %s: %s\n", resp.getStatusCode(), resp.getRawEntity(), resp.getEntity());

                } else if (cmd.equals("seg") && args.length >= 2) {
                    String fileName = args[1];
                    CloudFilesSegmentContainer sc = CloudFilesSegmentContainer.threadedSegmentFile(fileName, segmentSize);
                    printSegContainer(sc);
                } else if (cmd.equals("segup") && args.length >= 3) {
                    String containerName = args[1];
                    String filePath = args[2];
                    System.out.printf("Creating segments for container %s with segmentsize of %d\n\n", containerName, segmentSize);
                    ResponseContainer<Boolean> resp = cfu.createContainer(containerName);
                    System.out.printf("createContainer call yielded %s\n", resp.getRawEntity());
                    CloudFilesSegmentContainer sc = CloudFilesSegmentContainer.threadedSegmentFile(filePath, segmentSize);
                    printSegContainer(sc);
                    System.out.printf("uploadeding\n");
                    List<ResponseContainer<Boolean>> respList = cfu.writeSegmentContainer(containerName, sc);
                    for (ResponseContainer<Boolean> segResp : respList) {
                        System.out.printf("%d %s: %s\n", segResp.getStatusCode(), segResp.getRawEntity(), segResp.getEntity());
                    }
                } else if (cmd.equals("mkcnt") && args.length >= 2) {
                    String containerName = args[1];
                    ResponseContainer<Boolean> resp = cfu.createContainer(containerName);
                    System.out.printf("created container %s: %s\n", containerName, resp.getEntity());
                } else if (cmd.equals("segmentsize") && args.length >= 2) {
                    segmentSize = Integer.valueOf(args[1]);
                    System.out.printf("segmentsize set to %d\n", segmentSize);
                } else if (cmd.equals("rmc") && args.length >= 2) {
                    String containerName = args[1];
                    ResponseContainer<Integer> emptyResp = cfu.emptyContainer(containerName);
                    System.out.printf("Emptied container %s of %d files\n", containerName, emptyResp.getEntity());
                    ResponseContainer<Boolean> deleteResp = cfu.deleteContainer(containerName);
                    System.out.printf("%s\n", deleteResp.getEntity());
                } else if (cmd.equals("lc") && args.length >= 2) {
                    String containerName = args[1];
                    ResponseContainer<FilesObjectList> resp = cfu.listContainer(containerName);
                    FilesObjectList objectList = resp.getEntity();
                    System.out.printf("Container %s:\n", containerName);
                    long totalBytes = 0;
                    for (FilesObject fileObject : objectList.getList()) {
                        String sqlTime = StaticDateTimeUtils.toSqlTime(StaticDateTimeUtils.toDate(fileObject.getLastModified()));
                        String hash = fileObject.getHash();
                        String fileName = fileObject.getName();
                        long nBytes = fileObject.getBytes();
                        totalBytes += nBytes;
                        String nBytesStr = StaticStringUtils.lpadLong(nBytes, " ", 10);
                        System.out.printf("%s %s %s %s\n", hash, sqlTime, nBytesStr, fileName);
                    }
                    System.out.printf("Total bytes = %s\n", totalBytes);
                } else if (cmd.equals("auth")) {
                    ResponseContainer<AuthToken> token = cfu.getAuthToken();
                    System.out.printf("auth token = %s\n", cfu.getToken());
                } else if (cmd.equals("ls")) {
                    FilesContainerList resp = cfu.listContainers().getEntity();
                    List<FilesContainer> filesList = resp.getList();
                    for (FilesContainer fileContainer : filesList) {
                        int count = fileContainer.getCount();
                        String countStr = StaticStringUtils.lpadLong(count, " ", 4);
                        long nBytes = fileContainer.getBytes();
                        String nBytesStr = StaticStringUtils.lpadLong(nBytes, " ", 10);
                        String name = fileContainer.getName();
                        System.out.printf("   %s %s %s\n", countStr, nBytesStr, name);
                    }
                } else {
                    System.out.printf("Unknown cmd %s\n", cmdLine);
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException, ParseException {
        CloudFilesCli cli = new CloudFilesCli();
        cli.run(args);
    }

    private void printSegContainer(CloudFilesSegmentContainer sc) {
        System.out.printf("Container: %s\n", sc.getFileName());
        for (CloudFilesSegment seg : sc.getSegments()) {
            String fragStr = StaticStringUtils.lpadLong(seg.getFragNumber(), " ", 4);
            String hash = seg.getMd5sum();
            String fileName = seg.getFileName();
            String sizeStr = StaticStringUtils.lpadLong(seg.getSize(), " ", 10);
            System.out.printf("%s %s %s %s\n", fragStr, hash, sizeStr, fileName);
        }
    }
}
