package org.openstack.atlas.api.resources;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3.atom.Link;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class LoadBalancersResourceTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class WhenCreatingALoadBalancer {

        private LoadBalancersResource loadBalancersResource;
        private LoadBalancerService loadBalancerService;
        private AsyncService asyncService;
        private VirtualIpService virtualIpService;
        private LoadBalancer loadBalancer;

        @Before
        public void setUp() {
            asyncService = mock(AsyncService.class);
            virtualIpService = mock(VirtualIpService.class);
            loadBalancerService = mock(LoadBalancerService.class);
            loadBalancersResource = new LoadBalancersResource();
            loadBalancersResource.setAsyncService(asyncService);
            loadBalancersResource.setVirtualIpService(virtualIpService);
            loadBalancersResource.setLoadBalancerService(loadBalancerService);
            loadBalancersResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));
        }

        @Before
        public void setupLoadBalancerObject() {
            loadBalancer = new LoadBalancer();
            loadBalancer.setName("a-new-loadbalancer");
            loadBalancer.setProtocol("IMAPv4");

            List<VirtualIp> virtualIps = new ArrayList<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setType(VipType.PUBLIC);
            virtualIps.add(vip);

            loadBalancer.getVirtualIps().addAll(virtualIps);

            Nodes nodes = new Nodes();
            Node node = new Node();
            node.setAddress("10.1.1.1");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.getNodes().add(node);
            loadBalancer.getNodes().addAll(nodes.getNodes());
        }

        @Test
        public void shouldProduce400ResponseWhenFailingValidation() {
            Response response = loadBalancersResource.createLoadBalancer(new LoadBalancer());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce202ResponseWhenCreateSucceeds() throws Exception {
            doNothing().when(virtualIpService).addAccountRecord(Matchers.<Integer>any());
            when(loadBalancerService.create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any())).thenReturn(new org.openstack.atlas.service.domain.entities.LoadBalancer());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.CREATE_LOADBALANCER), Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduce500ResponseWhenCreateThrowsException() throws Exception {
            doThrow(Exception.class).when(loadBalancerService).create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500onJmsException() throws Exception {
            when(loadBalancerService.create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any())).thenReturn(new org.openstack.atlas.service.domain.entities.LoadBalancer());
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.CREATE_LOADBALANCER), Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest({PaginationHelper.class, MapperBuilder.class})
    @PowerMockIgnore("javax.management.*")
    public static class WhenRetrievingUsage{

        private LoadBalancersResource loadBalancersResource;
        private LoadBalancerService loadBalancerService;
        private AsyncService asyncService;
        private VirtualIpService virtualIpService;
        private LoadBalancer loadBalancer;
        private AccountBilling accountBilling;
        private AccountUsage accountUsage;
        private RestApiConfiguration restApiConfiguration;
        private String mockedBaseUri = "http://mocked.api.endpoint/v1.0";
        private PaginationHelper paginationHelper;
        
        @Before
        public void setUp() {
            asyncService = mock(AsyncService.class);
            virtualIpService = mock(VirtualIpService.class);
            loadBalancerService = mock(LoadBalancerService.class);
            restApiConfiguration = mock(RestApiConfiguration.class);
            PowerMockito.mockStatic(MapperBuilder.class);
            PowerMockito.mockStatic(PaginationHelper.class);
            loadBalancersResource = new LoadBalancersResource();
            loadBalancersResource.setAsyncService(asyncService);
            loadBalancersResource.setVirtualIpService(virtualIpService);
            loadBalancersResource.setLoadBalancerService(loadBalancerService);
            loadBalancersResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));

            when(PaginationHelper.createLink(anyString(), anyString(), anyBoolean())).thenReturn(new Link());


        }

        @Before
        public void setupLoadBalancerObject() {
            loadBalancersResource.setAccountId(5);
            loadBalancer = new LoadBalancer();
            loadBalancer.setName("a-new-loadbalancer");
            loadBalancer.setProtocol("IMAPv4");

            List<VirtualIp> virtualIps = new ArrayList<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setType(VipType.PUBLIC);
            virtualIps.add(vip);


            loadBalancer.getVirtualIps().addAll(virtualIps);

            Nodes nodes = new Nodes();
            Node node = new Node();
            node.setAddress("10.1.1.1");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.getNodes().add(node);
            loadBalancer.getNodes().addAll(nodes.getNodes());
        }

        @Test
        public void shouldProduce400ResponseForAccountLevelUsage() {
            Response response = loadBalancersResource.retrieveAccountBilling("2019-11-21T10:15:30", "2019-10-21T10:15:30", 0,100);
            Assert.assertEquals(400,response.getStatus());
        }

        @Test
        public void shouldProduce200ResponseForAccountLevelUsage() throws EntityNotFoundException {
            
            org.openstack.atlas.service.domain.pojos.AccountBilling abilling = new org.openstack.atlas.service.domain.pojos.AccountBilling();
            List<org.openstack.atlas.service.domain.entities.AccountUsage> accountUsageRecords = new ArrayList<>();
            abilling.setAccountUsageRecords(accountUsageRecords);

            
            when(loadBalancerService.getAccountBilling(anyInt(), any(Calendar.class), any(Calendar.class), anyInt(), anyInt())).thenReturn(abilling);

            Response response = loadBalancersResource.retrieveAccountBilling("2019-09-21T10:15:30", "2019-12-21T10:15:30", 1,100);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldProduce400ResponseForListBillable() {
            Response response = loadBalancersResource.retrieveBillableLoadBalancers("2019-11-21T10:15:30", "2019-10-21T10:15:30", 0,100);
            Assert.assertEquals(400,response.getStatus());
        }

        @Test
        public void shouldProduce200ResponseForListBillable(){
            Response response = loadBalancersResource.retrieveBillableLoadBalancers("2019-09-21T10:15:30", "2019-12-21T10:15:30", 1,100);
            Assert.assertEquals(200, response.getStatus());
        }


    }

    public static class WhenRetrievingResources {


        private LoadBalancerResource loadBalancerResource;
        Integer lbId;

        private LoadBalancersResource loadBalancersResource;



        @Before
        public void setUp() {
            loadBalancerResource = mock(LoadBalancerResource.class);

            lbId = 42;
            loadBalancersResource = new LoadBalancersResource();
            loadBalancersResource.setLoadBalancerResource(loadBalancerResource);

        }

        @Test
        public void shouldSetAccountIdAndLbIdForLoadBalancerResource() {
            loadBalancersResource.retrieveLoadBalancerResource(lbId);
            verify(loadBalancerResource).setId(lbId);
            verify(loadBalancerResource).setAccountId(ArgumentMatchers.<Integer>any());
        }
    }


    public static class WhenRetrievingLoadBalancers {

        private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

        @Mock
        private LoadBalancerService loadBalancerService;
        @InjectMocks
        private LoadBalancersResource loadBalancersResource;
        private org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer1;
        private org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer2;
        private List<org.openstack.atlas.service.domain.entities.LoadBalancer> loadBalancerList;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            loadBalancersResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));
            loadBalancer1 = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            loadBalancer1.setName("first-loadBalancer");
            loadBalancer1.setAccountId(1);
            loadBalancer1.setPort(8080);

            loadBalancer2 = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            loadBalancer2.setName("first-loadBalancer");
            loadBalancer2.setAccountId(1);
            loadBalancer2.setPort(8081);

            loadBalancerList = new ArrayList<>();
            loadBalancerList.add(loadBalancer1);
            loadBalancerList.add(loadBalancer2);

            doReturn(loadBalancerList).when(loadBalancerService).getLoadbalancersByName(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
        }

        @Test
        public void ShouldReturnAListOfLoadBalancersIdentifiedWithTheName() {
        Response response = loadBalancersResource.retrieveLoadBalancers("first-loadBalancer", 0, 99);
        Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void ShouldThrowBadRequestExceptionWhenNamePassedIsEmptyString() {
            String expected = "Must supply LoadBalancer name to process this request.";
            Response response = loadBalancersResource.retrieveLoadBalancers("", 0, 99);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals(expected, ((BadRequest)response.getEntity()).getMessage());
        }

        @Test
        public void ShouldThrowBadRequestExceptionWhenNamePassedIsNull() {
            String expected = "Must supply LoadBalancer name to process this request.";
            Response response = loadBalancersResource.retrieveLoadBalancers(null, 0, 99);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals(expected, ((BadRequest)response.getEntity()).getMessage());
        }

        @Test
        public void ShouldThrowException() {
          doThrow(Exception.class).when(loadBalancerService).getLoadbalancersByName(ArgumentMatchers.anyString(),
                  ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            Response response = loadBalancersResource.retrieveLoadBalancers("first-loadBalancer", 0, 99);
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
