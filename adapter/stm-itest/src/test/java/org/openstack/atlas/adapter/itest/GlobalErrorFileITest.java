package org.openstack.atlas.adapter.itest;


import org.apache.axis.AxisFault;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.util.Constants;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;

public class GlobalErrorFileITest extends STMTestBase {
    String pageContent = "ERROR PAGE CONTENT";
    String vsName;


    //TODO: needs more testing for lb and its error files..
    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() throws RollBackException, InsufficientRequestException, RemoteException {
        stmAdapter.deleteLoadBalancer(config, lb);
    }

    @Test
    public void testSimpleErrorFileOperations() throws Exception {
        setCustomErrorFile();
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testSimpleDeleteErrorFileOperations() throws Exception {
        setCustomErrorFile();
        deleteErrorFile();
    }

    private void setCustomErrorFile() throws Exception {
        vsName = ZxtmNameBuilder.genVSName(lb);

        //This is mgmt call to set 'default' file other than stm Default, lb should have Default at this point.
        stmAdapter.uploadDefaultErrorFile(config, pageContent);
        File file = stmClient.getExtraFile(Constants.DEFAULT_ERRORFILE);
        Assert.assertNotNull(file);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String content = reader.readLine();
        Assert.assertEquals(pageContent, content);
    }

    //TODO: need to add delete to adapter. This is a 'global' file and only used by ops(which they have yet to ever use)
    private void deleteErrorFile() throws RollBackException, AxisFault, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
//        stmAdapter.deleteErrorFile(config, lb);
//        stmClient.getExtraFile(errorFileName());
    }
}
