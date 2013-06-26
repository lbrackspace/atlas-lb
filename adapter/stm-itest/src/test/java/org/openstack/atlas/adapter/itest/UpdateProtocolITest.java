package org.openstack.atlas.adapter.itest;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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

    }

    @Test
    public void updateProtocolToHTTP() {
        try{
            stmAdapter.updateProtocol();



        }catch(Exception e){}

    }





}
