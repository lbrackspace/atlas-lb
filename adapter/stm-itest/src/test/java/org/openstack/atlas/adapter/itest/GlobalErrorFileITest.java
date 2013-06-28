package org.openstack.atlas.adapter.itest;


import org.apache.axis.AxisFault;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;

public class GlobalErrorFileITest extends STMTestBase {
    String vsName;

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

    @Test(expected = StingrayRestClientException.class)
    public void testSimpleErrorFileOperations() throws Exception {
        setCustomErrorFile();
        deleteErrorFile();

    }

    private void setCustomErrorFile() throws Exception {
        String pageContent = "ERROR PAGE CONTENT";
        vsName = ZxtmNameBuilder.genVSName(lb);

        stmAdapter.setErrorFile(config, lb, pageContent);
        File file = stmClient.getExtraFile(vsName);
        Assert.assertNotNull(file);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String content = reader.readLine();
        Assert.assertEquals(pageContent, content);




    }

    private void deleteErrorFile() throws RollBackException, AxisFault, InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        stmAdapter.deleteErrorFile(config, lb);
        stmClient.getExtraFile(vsName);

    }


}
