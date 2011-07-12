package org.openstack.atlas.util;

import org.openstack.atlas.test.BaseHadoopTest;
import org.openstack.atlas.test.TestHelper;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.util.FileSystemUtils;

import java.io.*;

@Ignore
public class FileSystemUtilsTest extends BaseHadoopTest {
    private FileSystemUtils fileSystemUtils = new FileSystemUtils();

    private String currentdir;

    private String localFilename = "upload_to_dfs.txt";

    private String localpath;

    private String multipath;

    private String remoteMultipath;

    private String remotepath;

    private String uploadFilename = "uploaded.txt";

    public FileSystemUtilsTest() throws IOException {
        super();
        currentdir = System.getProperty("user.dir");
        currentdir = TestHelper.sanitizeCurrentDir(currentdir);
        localpath = currentdir + "/src/test/hadoop/input/dfs_upload_test/";
        File f = new File(localpath);
        f.mkdir();
        f = new File(localpath + "/upload_to_dfs.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write("I am going to be uploaded.\n" + "im a happy upload file.\n" + "\nhappy indeed.\n");
        out.close();
        multipath = currentdir + "/src/test/hadoop/input/dfs_multi_test/";
        f = new File(multipath);
        f.mkdir();
        createFile(multipath + "/upload_to_dfs1.txt");
        createFile(multipath + "/upload_to_dfs2.txt");
        createFile(multipath + "/upload_to_dfs3.txt");
        createFile(multipath + "/upload_to_dfs4.txt");
        createFile(multipath + "/upload_to_dfs5.txt");
        remotepath = currentdir + "/src/test/hadoop/dfs/test/upload/";
        remoteMultipath = currentdir + "/src/test/hadoop/dfs/test/multi_upload/";
        // make sure the System.getProperty returns the proper value. Its not
        // for mvn tests on the children projects.

    }

    @Test
    public void testDirSanitize() throws Exception {
        String saneDir = "thisisatest";
        String wackyDir = "this-*isa-*test";
        Assert.assertEquals(saneDir, fileSystemUtils.sanitizeDir(wackyDir));
        Assert.assertEquals(saneDir, fileSystemUtils.sanitizeDir(saneDir));
    }

    // private String sanitizeCurrentDir(String currentdir) {
    // int lastIndexOfSlash = currentdir.lastIndexOf("/");
    // if (currentdir.substring(lastIndexOfSlash + 1).startsWith("stats-") ) {
    // // assume its in a child project of the main project, chop it off
    // currentdir = currentdir.substring(0, lastIndexOfSlash + 1);
    // }
    // //
    // return currentdir;
    //  
    // }

    @Test
    public void testFileLength() throws Exception {
        fileSystemUtils.putFileArgsOntoDFS(null, null, new String[]{});
    }

    @Test
    public void testFullFilename() {
        String beginStr = "baz";
        String longStr = "/foo/bar/" + beginStr;
        Assert.assertEquals(beginStr, fileSystemUtils.getRestOfFilename(longStr));
        Assert.assertEquals(beginStr, fileSystemUtils.getRestOfFilename(beginStr));
    }

    @Test
    public void testMakeDirs() throws Exception {
        String dfsPath = remotepath + "/foo/";
        fileSystemUtils.makeDirectories(getFileSystem(), dfsPath);
        Assert.assertNotNull(fileSystemUtils.ls(getFileSystem(), remotepath)[0]);
        File f = new File(dfsPath);
        f.delete();
    }

    @Test
    public void testPlaceFilesOnDFS() throws Exception {
        String dfsPath = remotepath + uploadFilename;
        fileSystemUtils.placeFileOnDFS(getFileSystem(), localpath + localFilename, dfsPath);
        Assert.assertNotNull(fileSystemUtils.ls(getFileSystem(), dfsPath)[0]);
        fileSystemUtils.copyToLocalFile(getFileSystem(), new Path(dfsPath), new Path(localpath
            + uploadFilename));
        File f = new File(localpath + uploadFilename);
        Assert.assertTrue(f.exists());
    }

    @Test
    public void testReadFilesFromDFS() throws Exception {
        String dfsPath = remotepath + uploadFilename;
        fileSystemUtils.placeFileOnDFS(getFileSystem(), localpath + localFilename, dfsPath);
        Assert.assertNotNull(fileSystemUtils.ls(getFileSystem(), dfsPath)[0]);

        FSDataInputStream in = fileSystemUtils.readFileFromDFS(getFileSystem(), dfsPath);
        readAndClose(in);

        in = fileSystemUtils.readFileFromDFS(getFileSystem(), new Path(dfsPath));
        readAndClose(in);
    }

    @Test
    public void testSanitizeDir() throws Exception {
        String fullDir = "/path/to/";
        String statsName = "stats-funky";
        Assert.assertEquals(fullDir, TestHelper.sanitizeCurrentDir(fullDir + statsName));
        Assert.assertEquals(fullDir, TestHelper.sanitizeCurrentDir(fullDir));
    }

    @Test
    public void testUploadMulti() throws Exception {
        String[] files = new String[5];
        for (int i = 0; i < files.length; i++) {
            files[i] = multipath + "/upload_to_dfs" + (i + 1) + ".txt";
        }
        fileSystemUtils.putFileArgsOntoDFS(getFileSystem(), remoteMultipath, files);
        fileSystemUtils.ls(getFileSystem(), remoteMultipath);
        for (int i = 0; i < files.length; i++) {
            fileSystemUtils.removeFileFromDFS(getFileSystem(), remoteMultipath + "/upload_to_dfs" + (i + 1)
                + ".txt", false);
        }
    }

    @Test
    public void testUploadMultiViaDirectory() throws Exception {
        String[] files = new String[1];
        files[0] = multipath;
        fileSystemUtils.putFileArgsOntoDFS(getFileSystem(), remoteMultipath, files);
        fileSystemUtils.ls(getFileSystem(), remoteMultipath);
        for (int i = 0; i < 5; i++) {
            fileSystemUtils.removeFileFromDFS(getFileSystem(), remoteMultipath + "/upload_to_dfs" + (i + 1)
                + ".txt", false);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fileSystemUtils.removeFileFromDFS(getFileSystem(), remotepath + uploadFilename, true);
        File f = new File(localpath + uploadFilename);
        if (f.exists()) {
            f.delete();
        }
    }

    private void createFile(String filename) throws IOException {
        File f;
        f = new File(filename);
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write("I am going to be uploaded.\n" + "im a happy upload file.\n" + "\nhappy indeed.\n");
        out.close();
    }

    private void readAndClose(FSDataInputStream in) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(in));
        String bufferedContents = "";
        String line = d.readLine();
        do {
            bufferedContents += line + "\n";
            line = d.readLine();
        } while (line != null);

        Assert.assertNotNull(bufferedContents);
        Assert.assertFalse(bufferedContents.isEmpty());
        d.close();
    }
}
