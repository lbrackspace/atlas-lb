package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.util.Constants;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.io.File;
import java.util.Scanner;

public class GlobalErrorFileITest extends STMTestBase {
    String defaultPageContent = "DEFAULT ERROR PAGE CONTENT";
    String customPageContent = "CUSTOM ERROR PAGE CONTENT";
    String customPageContent2 = "CUSTOM ERROR PAGE CONTENT 2";
    String vsName;


    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
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

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testSimpleDeleteErrorFileOperations() throws Exception {
        setCustomErrorFile();
        deleteErrorFile();
    }

    private void setDefaultErrorFile() throws Exception {
        //vsName = ZxtmNameBuilder.genVSName(lb);

        //This is mgmt call to set 'default' file other than stm Default, lb should have Default at this point.
        stmAdapter.uploadDefaultErrorFile(config, defaultPageContent);
        File file = stmClient.getExtraFile(Constants.DEFAULT_ERRORFILE);
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

        stmAdapter.setErrorFile(config, lb, customPageContent);
        File file = stmClient.getExtraFile(errorFileName());
        reader = new Scanner(file);
        content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        Assert.assertEquals(customPageContent, content);

        stmAdapter.setErrorFile(config, lb, customPageContent2);
        file = stmClient.getExtraFile(errorFileName());
        reader = new Scanner(file);
        content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        Assert.assertEquals(customPageContent2, content);
    }

    private void deleteErrorFile() throws InsufficientRequestException, StmRollBackException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        final String errorFileName = errorFileName();
        File errorFile = stmClient.getExtraFile(errorFileName);

        Assert.assertNotNull(errorFile);

        stmAdapter.deleteErrorFile(config, lb);
        String errorFileNameLocal = stmClient.getVirtualServer(vsName).getProperties().getConnectionErrors().getErrorFile();
        Assert.assertEquals("Default", errorFileNameLocal);
        stmClient.getExtraFile(errorFileName); //expect ONFException
    }
}
