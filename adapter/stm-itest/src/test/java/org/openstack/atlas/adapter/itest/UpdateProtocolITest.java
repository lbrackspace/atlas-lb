package org.openstack.atlas.adapter.itest;


import com.zxtm.service.client.VirtualServerProtocol;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.rackspace.stingray.client.StingrayRestClient;

import java.rmi.RemoteException;

public class UpdateProtocolITest extends STMTestBase{


    @BeforeClass
    public static void setupClass() throws InterruptedException {
       Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass()
    {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Test
    public void updateProtocolToAndFromHTTP() {
        try{
            String vsName;
            vsName = ZxtmNameBuilder.genVSName(lb);
            StingrayRestClient client = new StingrayRestClient();
            Assert.assertEquals(VirtualServerProtocol.http.toString().toUpperCase(), client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
            stmAdapter.updateProtocol(config, lb);
            boolean isConnectionLogging = true;
            lb.setConnectionLogging(isConnectionLogging);
            Assert.assertEquals(LoadBalancerProtocol.HTTPS.toString().toUpperCase(),client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            Assert.assertNotNull(client.getVirtualServer(vsName).getProperties().getLog());
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            stmAdapter.updateProtocol(config, lb);
            Assert.assertEquals(LoadBalancerProtocol.HTTP.toString().toUpperCase(), client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getLog().getEnabled());

        }catch(Exception e){}

    }






}
