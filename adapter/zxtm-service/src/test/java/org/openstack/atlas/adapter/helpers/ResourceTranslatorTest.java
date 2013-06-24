package org.openstack.atlas.adapter.helpers;


import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.stm.STMTestBase;
import org.openstack.atlas.adapter.stm.StmAdapterImpl;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.rackspace.stingray.client.virtualserver.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class ResourceTranslatorTest extends STMTestBase{




    public static class WhenTranslatingAVirtualServer {


        String vsName;
        Boolean isHalfClosed;
        Boolean isConnectionLogging;
        Boolean isContentCaching;
        String logFormat;
        VirtualServerTcp expectedTcp;
        VirtualServerConnectionError expectedError;
        List<String> rules;
        ResourceTranslator translator;
        String errorFile;




        public void initializeVars(String logFormat, LoadBalancerProtocol protocol) {
            setupIvars();
            createSimpleLoadBalancer();
            vsName = "test_name";
            this.isConnectionLogging = true;
            isHalfClosed = true;
            isContentCaching = true;
            expectedTcp = new VirtualServerTcp();
            expectedError = new VirtualServerConnectionError();
            errorFile = "Default";
            expectedError.setError_file(errorFile);

            rules = java.util.Arrays.asList(StmAdapterImpl.XFF, StmAdapterImpl.XFP);
            translator = new ResourceTranslator();

            ConnectionLimit connectionLimit = new ConnectionLimit();
            Set<AccessList> accessListSet = new HashSet<AccessList>();
            accessListSet.add(new AccessList());
            this.logFormat = logFormat;


            lb.setConnectionLogging(isConnectionLogging);
            lb.setHalfClosed(isHalfClosed);
            lb.setContentCaching(isContentCaching);
            lb.setProtocol(protocol);
            lb.setConnectionLimit(connectionLimit);
            lb.setAccessLists(accessListSet);
        }

         public void initializeVars(String logFormat, LoadBalancerProtocol protocol, String errorFile)
         {

             initializeVars(logFormat, protocol);
             this.errorFile = errorFile;
             UserPages userPages = new UserPages();
             userPages.setErrorpage(this.errorFile);
             lb.setUserPages(userPages);
             expectedError.setError_file(this.errorFile);


         }


        @Test
        public void shouldCreateValidVirtualServer() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxy_close(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWeb_cache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(lb.getProtocol().name(), createdBasic.getProtocol());
            Assert.assertEquals(lb.getPort(), createdBasic.getPort());
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtection_class());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListen_on_any());
            Assert.assertEquals(rules, createdBasic.getRequest_rules());
            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());
        }

        @Test
        public void checkAlternateValidPaths() throws InsufficientRequestException
        {
            initializeVars("%v %t %h %A:%p %n %B %b %T", LoadBalancerProtocol.FTP, "HI");
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerLog log = createdProperties.getLog();
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());




        }


    }
}
