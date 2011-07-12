package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
@Ignore
public class RateLimitResourceTest {
    public static class WhenCreatingRateLimit {
        private RateLimitResource rateLimitResource;
        private RateLimit rateLimit;
        private ManagementAsyncService asyncService;
        private OperationResponse operationResponse;
        private LoadBalancerRepository lbRepository;

        @Before
        public void setUp() {

            asyncService = mock(ManagementAsyncService.class);
            rateLimitResource = new RateLimitResource();
            rateLimitResource.setManagementAsyncService(asyncService);
            rateLimitResource.setMockitoAuth(true);
            lbRepository = mock(LoadBalancerRepository.class);
            rateLimitResource.setLoadBalancerRepository(lbRepository);
            operationResponse = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
            rateLimitResource.setDozerMapper(new DozerBeanMapper(mappingFiles));

        }

        @Before
        public void standUpRateLimitObject() {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat badInputFormat = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");

            Date date1 = null;
            try {
                date1 = inputFormat.parse("2015-10-17T00:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(date1);
            Ticket ticket = new Ticket();
            ticket.setTicketId("1234");
            ticket.setComment("My first comment!");
            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(3);
            rateLimit.setTicket(ticket);
            rateLimit.setExpirationTime(cal);
        }

        @Test
        public void shouldProduce400ResponseWhenFailingValidation() {
            Response response = rateLimitResource.createRateLimit(new RateLimit());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {
            Response resp = rateLimitResource.createRateLimit(rateLimit);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseFailed() throws Exception {
            operationResponse.setExecutedOkay(false);
            Response response = rateLimitResource.createRateLimit(rateLimit);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn202OnEsbReturnOk() throws Exception {
            operationResponse.setExecutedOkay(true);            
            Response response = rateLimitResource.createRateLimit(rateLimit);
            Assert.assertEquals(202, response.getStatus());
        }
    }
}
