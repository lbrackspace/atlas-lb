package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.util.Constants;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.io.File;
import java.util.Scanner;

public class GlobalErrorFileITest extends VTMTestBase {
    String defaultPageContent = "DEFAULT ERROR PAGE CONTENT";
    String customPageContent = "CUSTOM ERROR PAGE CONTENT";
    String customPageContent2 = "CUSTOM ERROR PAGE CONTENT 2";
    String vsName;


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    @Test
    public void testDefaultErrorFileOperations() throws Exception {
        setDefaultErrorFile();
    }

    @Test
    public void testCustomErrorFileOperations() throws Exception {
        setCustomErrorFile();
    }

    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testSimpleDeleteErrorFileOperations() throws Exception {
        setCustomErrorFile();
        deleteErrorFile();
    }

    private void setDefaultErrorFile() throws Exception {
        //vsName = ZxtmNameBuilder.genVSName(lb);

        //This is mgmt call to set 'default' file other than stm Default, lb should have Default at this point.
        vtmAdapter.uploadDefaultErrorFile(config, defaultPageContent);
        File file = vtmClient.getExtraFile(Constants.DEFAULT_ERRORFILE);
        Assert.assertNotNull(file);

        Scanner reader = new Scanner(file);
        String content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        Assert.assertEquals(defaultPageContent, content);
    }

    private void setCustomErrorFile() throws Exception {
        vsName = ZxtmNameBuilder.genVSName(lb);
        Scanner reader;
        String content;
        Assert.assertFalse(customPageContent.equals(customPageContent2)); //assert our assumption

        vtmAdapter.setErrorFile(config, lb, customPageContent);
        File file = vtmClient.getExtraFile(errorFileName());
        reader = new Scanner(file);
        content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        Assert.assertEquals(customPageContent, content);

        vtmAdapter.setErrorFile(config, lb, customPageContent2);
        file = vtmClient.getExtraFile(errorFileName());
        reader = new Scanner(file);
        content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        Assert.assertEquals(customPageContent2, content);
    }

    private void deleteErrorFile() throws InsufficientRequestException, StmRollBackException, VTMRestClientException, VTMRestClientObjectNotFoundException {
        final String errorFileName = errorFileName();
        File errorFile = vtmClient.getExtraFile(errorFileName);

        Assert.assertNotNull(errorFile);

        vtmAdapter.deleteErrorFile(config, lb);
        String errorFileNameLocal = vtmClient.getVirtualServer(vsName).getProperties().getConnectionErrors().getErrorFile();
        Assert.assertEquals("Default", errorFileNameLocal);
        vtmClient.getExtraFile(errorFileName); //expect ONFException
    }
}
