package org.openstack.atlas.cloudfiles.itest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.cloudfiles.CloudFilesConfig;
import org.openstack.atlas.cloudfiles.CloudFilesUtils;
import org.openstack.atlas.cloudfiles.SegmentMd5Thread;
import org.openstack.atlas.cloudfiles.objs.AuthToken;
import org.openstack.atlas.util.common.CloudFilesSegment;
import org.openstack.atlas.cloudfiles.objs.FilesContainer;
import org.openstack.atlas.cloudfiles.objs.FilesContainerList;
import org.openstack.atlas.cloudfiles.objs.FilesObject;
import org.openstack.atlas.cloudfiles.objs.FilesObjectList;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.util.common.SegmentedInputStream;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.debug.SillyTimer;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class CloudFilesCli {

    private BufferedReader stdin;
    private CloudFilesConfig cfCfg;
    private HadoopLogsConfigs hdCfg;
    private CloudFilesUtils cfu;
    private int segmentSize;
    private static final int BUFFSIZE = 64 * 1024;

    public void run(String[] argv) throws FileNotFoundException, UnsupportedEncodingException, IOException, ParseException {
        hdCfg = HadoopLogsConfigs.getInstance();
        //cfCfg = new CloudFilesConfig(CloudFilesConfig.readJsonConfig("~/cloudFiles.json"));
        cfCfg = new CloudFilesConfig();
        cfu = new CloudFilesUtils(cfCfg);
        segmentSize = cfCfg.getSegmentSize();
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
                    System.out.printf("segdown <cnt> <filePath>    #Download segmented container\n");
                    System.out.printf("up <cnt> <obj> <filePath>   #upload single file\n");
                    System.out.printf("down <cnt> <obj> <filePath> #download single file\n");
                    System.out.printf("bulk <fileName> <size>      #write junk bytes to file for testing\n");
                    System.out.printf("ec <cnt>                    #delete all objects in container\n");
                    System.out.printf("nuke <all>                  #delete all containers Dangours\n");
                    System.out.printf("bulkread <filePath>         #Read file to dev null\n");
                    System.out.printf("showConfig                  #Show configuration\n");
                } else if (cmd.equals("segdown") && args.length >= 2) {
                    String containerName = args[1];
                    String filePath = args[2];
                    FileOutputStream os = null;
                    long total_bytes = 0;
                    ResponseContainer<FilesObjectList> cntResp = cfu.listContainer(containerName);
                    for (FilesObject fileObj : cntResp.getEntity().getList()) {
                        total_bytes += fileObj.getBytes();
                    }
                    System.out.printf("Attempting to read %d bytes\n", total_bytes);
                    os = new FileOutputStream(new File(StaticFileUtils.expandUser(filePath)));
                    SillyTimer timer = new SillyTimer();
                    timer.restart();
                    try {
                        List<ResponseContainer<Boolean>> respList = cfu.readSegmentContainer(containerName, os);
                        for (ResponseContainer<Boolean> resp : respList) {
                            System.out.printf("%s\n", resp.toString());
                        }
                    } catch (Exception ex) {
                        throw ex;
                    } finally {
                        StaticFileUtils.close(os);
                    }
                    double seconds = timer.readSeconds();
                    System.out.printf("Took %f seconds to read %d bytes\n", seconds, total_bytes);
                    System.out.printf("rate is %s bytes per seconds\n", Debug.humanReadableBytes(total_bytes / seconds));

                } else if (cmd.equals("showConfig")) {

                    System.out.printf("hadoopLogsConfigs = %s\n", HadoopLogsConfigs.staticToString());
                    System.out.printf("CloudFilesConfig = %s\n", cfCfg.toString());
                    System.out.printf("Current segmentsize = %d\n", segmentSize);
                } else if (cmd.equals("bulkread") && args.length >= 2) {
                    String filePath = args[1];
                    byte[] buff = new byte[BUFFSIZE];
                    File file = new File(StaticFileUtils.expandUser(filePath));
                    long fileLength = file.length();
                    //InputStream is = new FileInputStream(file);
                    InputStream is = new SegmentedInputStream(file, 0, fileLength);
                    SillyTimer timer = new SillyTimer();
                    timer.restart();
                    int nBytes;
                    while (true) {
                        nBytes = is.read(buff, 0, BUFFSIZE);
                        if (nBytes < 0) {
                            break;
                        }
                    }
                    double seconds = timer.readSeconds();
                    is.close();
                    System.out.printf("Took %f seconds to read %d bytes\n", seconds, fileLength);
                    System.out.printf("rate = %s bytes per seconds\n", Debug.humanReadableBytes(fileLength / seconds));
                } else if (cmd.equals("nuke") && args.length >= 2 && args[1].equals("all")) {
                    ResponseContainer<FilesContainerList> containers = cfu.listContainers();
                    List<FilesContainer> containerList = containers.getEntity().getList();
                    for (FilesContainer cnt : containerList) {
                        String containerName = cnt.getName();
                        System.out.printf("Delting container %s: ", containerName);
                        System.out.flush();
                        ResponseContainer<Integer> emptyResp = cfu.emptyContainer(containerName);
                        System.out.printf("%d files: ", emptyResp.getEntity());
                        System.out.flush();
                        ResponseContainer<Boolean> deleteResponse = cfu.deleteContainer(containerName);
                        System.out.printf("%s\n", deleteResponse);
                    }
                } else if (cmd.equals("ec") && args.length >= 2) {
                    String containerName = args[1];
                    System.out.printf("Deleting objects from  container %s: ", containerName);
                    System.out.flush();
                    ResponseContainer<Integer> resp = cfu.emptyContainer(containerName);
                    System.out.printf("%s\n", resp);
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
                    os.close();
                    System.out.printf("\n");
                } else if (cmd.equals("down") && args.length >= 4) {
                    String containerName = args[1];
                    String objectName = args[2];
                    String filePath = args[3];
                    System.out.printf("Downloading container %s object %s to file %s\n", containerName, objectName, filePath);
                    ResponseContainer<Boolean> resp = cfu.readObject(containerName, objectName, filePath);
                    System.out.printf("%s\n", resp);
                } else if (cmd.equals("up") && args.length >= 4) {
                    String containerName = args[1];
                    String objectName = args[2];
                    String filePath = args[3];
                    System.out.printf("Writing file %s to container %s as object %s\n", filePath, containerName, objectName);
                    ResponseContainer<Boolean> resp = cfu.writeObject(containerName, objectName, filePath, null);
                    System.out.printf("%s\n", resp);

                } else if (cmd.equals("seg") && args.length >= 2) {
                    String fileName = args[1];
                    CloudFilesSegmentContainer sc = SegmentMd5Thread.threadedSegmentFile(fileName, segmentSize);
                    printSegContainer(sc);
                } else if (cmd.equals("segup") && args.length >= 3) {
                    String containerName = args[1];
                    String filePath = args[2];
                    System.out.printf("Creating segments for container %s with segmentsize of %d\n\n", containerName, segmentSize);
                    ResponseContainer<Boolean> resp = cfu.createContainer(containerName);
                    System.out.printf("createContainer call yielded %s\n", resp.toString());
                    long fileSize = StaticFileUtils.fileSize(filePath);
                    CloudFilesSegmentContainer sc = SegmentMd5Thread.threadedSegmentFile(filePath, segmentSize);
                    printSegContainer(sc);
                    System.out.printf("uploadeding\n");
                    SillyTimer timer = new SillyTimer();
                    timer.restart();
                    List<ResponseContainer<Boolean>> respList = cfu.writeSegmentContainer(containerName, sc);
                    double seconds = timer.readSeconds();
                    System.out.printf("Took %f seconds to send %d bytes\n", seconds, fileSize);
                    System.out.printf("transfer rate is %s\n", Debug.humanReadableBytes(fileSize / seconds));
                    for (ResponseContainer<Boolean> segResp : respList) {
                        System.out.printf("%s\n", segResp);
                    }
                } else if (cmd.equals("mkcnt") && args.length >= 2) {
                    String containerName = args[1];
                    ResponseContainer<Boolean> resp = cfu.createContainer(containerName);
                    System.out.printf("created container %s: %s\n", containerName, resp);
                } else if (cmd.equals("segmentsize") && args.length >= 2) {
                    segmentSize = Integer.valueOf(args[1]);
                    System.out.printf("segmentsize set to %d\n", segmentSize);
                } else if (cmd.equals("rmc") && args.length >= 2) {
                    String containerName = args[1];
                    ResponseContainer<Integer> emptyResp = cfu.emptyContainer(containerName);
                    System.out.printf("Emptied container %s of %d files\n", containerName, emptyResp.getEntity());
                    ResponseContainer<Boolean> deleteResp = cfu.deleteContainer(containerName);
                    System.out.printf("%s\n", deleteResp);
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
                        String nBytesStr = StaticStringUtils.lpadLong(nBytes, " ", 14);
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
