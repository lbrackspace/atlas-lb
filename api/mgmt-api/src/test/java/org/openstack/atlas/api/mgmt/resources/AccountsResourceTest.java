package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountBillings;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecords;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.GeneralFault;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LbaasFault;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.AccountBilling;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.GroupRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.w3.atom.Link;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AccountsResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenRetrievingAccountDetails {
        private ManagementAsyncService asyncService;
        private AccountsResource accountsResource;
        private OperationResponse operationResponse;
        private AccountResource accountResource;
        private LoadBalancerService lbService;
        private GroupRepository groupRepository;

        @Before
        public void setUp() {
            accountsResource = new AccountsResource();
            accountResource = new AccountResource();
            accountResource.setMockitoAuth(true);
            accountResource.setId(12);
            accountsResource.setAccountResource(accountResource);

            asyncService = mock(ManagementAsyncService.class);
            lbService = mock(LoadBalancerService.class);
            accountResource.setLoadBalancerService(lbService);
            groupRepository = mock(GroupRepository.class);
            accountResource.setGroupRepository(groupRepository);
            accountResource.setManagementAsyncService(asyncService);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            accountResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturnAccountResource() throws Exception {
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Assert.assertEquals(12, resource.getId());
        }

        @Test
        public void shouldReturnAccountLoadBalancers() throws Exception {
            List<AccountLoadBalancer> elbs = new ArrayList<AccountLoadBalancer>();
            AccountLoadBalancer elb = new AccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getAccountLoadBalancers(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnExtendedAccountLoadBalancers() throws Exception {
            List<ExtendedAccountLoadBalancer> elbs = new ArrayList<ExtendedAccountLoadBalancer>();
            ExtendedAccountLoadBalancer elb = new ExtendedAccountLoadBalancer();
            elb.setClusterId(2);
            elbs.add(elb);

            when(lbService.getExtendedAccountLoadBalancer(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveExtendedAccountLoadBalancers();

            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnGroups() throws Exception {
            List<GroupRateLimit> elbs = new ArrayList<GroupRateLimit>();
            GroupRateLimit elb = new GroupRateLimit();
            elb.setDescription("Description");
            elbs.add(elb);

            when(groupRepository.getByAccountId(12)).thenReturn(elbs);
            AccountResource resource = accountsResource.retrieveAccountResource(12);
            Response response = resource.retrieveGroups();

            Assert.assertEquals(200, response.getStatus());
        }

        @Ignore
        @Test
        public void shouldDeleteGroup() throws Exception {
            AccountResource resource = accountsResource.retrieveAccountResource(12);
//            Response response = resource.deleteAccountGroup();
            verify(groupRepository).deleteAllForAccount(12);

//            Assert.assertEquals(200, response.getStatus());
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockRunnerDelegate(MockitoJUnitRunner.class)
    @PrepareForTest({AccountsResource.class, DateTimeConverters.class, PaginationHelper.class})
    public static class RetrieveAllAccountUsage {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private AccountUsageRepository accountUsageRepository;
        @InjectMocks
        private AccountsResource accountsResource;
        private Link link;
        private Calendar startTime;
        private Calendar endTime;
        private AccountUsage accountUsage1;
        private AccountUsage accountUsage2;
        private List<AccountUsage> accountUsagesList;
        private String startTimeParam;
        private String endTimeParam;
        private Integer offset;
        private Integer limit;
        private String relativeUri;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            accountsResource = new AccountsResource();
            accountsResource.setMockitoAuth(true);
            accountsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            accountsResource.setAccountUsageRepository(accountUsageRepository);

            accountUsage1 = new AccountUsage();
            accountUsage2 = new AccountUsage();
            accountUsage1.setAccountId(8654);
            accountUsage1.setId(1);
            accountUsage1.setStartTime(startTime);
            accountUsage2.setAccountId(7654);
            accountUsage2.setId(2);
            accountUsage2.setStartTime(startTime);
            accountUsagesList = new ArrayList<AccountUsage>();
            accountUsagesList.add(accountUsage1);
            accountUsagesList.add(accountUsage2);
            link = new Link();
            link.setTitle("https://localhost/management/accounts/usage?startTime=2021-05-04T13:00:00-05:00&endTime=2021-05-05T14:00:00-05:00&offset=500&limit=1000");
            startTime = new GregorianCalendar(2021,05,04);
            endTime = new GregorianCalendar(2021,05,05);
            startTimeParam = "2021-05-04T13:00:00-05:00";
            endTimeParam = "2021-05-05T14:00:00-05:00";
            relativeUri = "https://localhost/management/accounts/usage?startTime=2021-05-04T13:00:00-05:00&endTime=2021-05-05T14:00:00-05:00&offset=500&limit=1000";
        }

        @Test
        public void shouldThrowBadRequestException400(){
            startTimeParam = null;
            endTimeParam = null;
            String expected = "\'startTime\' and \'endTime\' query parameters are required";
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            Assert.assertEquals(400, response.getStatus());
            String actual = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldThrowConverterException() {
            startTimeParam = "2021-05-0514:00:00-05:00";
            endTimeParam = "2021-05-0413:00:00-05:00";
            String actual = "Date parameters must follow ISO-8601 (yyyy-MM-dd'T'HH:mm:ss) format";
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            String expected = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldThrowEntityNotFoundException() {
            startTimeParam = "2021-05-05T13:00:00-05:00";
            endTimeParam = "2021-05-05T14:00:00-05:00";
            doThrow(EntityNotFoundException.class).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            Assert.assertEquals("Object not Found", ((LbaasFault)response.getEntity()).getMessage());
        }

        @Test
        public void shouldRetrieveAccountUsageWithOffsetZeroAndRecordsLessThanLimit() throws ConverterException {
            Integer expected = 2;
            PowerMockito.mockStatic(PaginationHelper.class);
            PowerMockito.mockStatic(DateTimeConverters.class);
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
            when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(100);
            when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(0);
            doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            Assert.assertEquals(200, response.getStatus());
            Integer actual = ((AccountUsageRecords)response.getEntity()).getAccountUsageRecords().size();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldRetrieveAccountUsageWithOffsetZeroAndRecordsGreaterThanLimit() throws ConverterException {
            Integer expected = 1;
            PowerMockito.mockStatic(PaginationHelper.class);
            PowerMockito.mockStatic(DateTimeConverters.class);
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
            when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(1);
            when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(0);
            doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            Assert.assertEquals(200, response.getStatus());
            Integer actual = ((AccountUsageRecords)response.getEntity()).getAccountUsageRecords().size();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldRetrieveAccountUsageWithOffsetGreaterThanZeroAndRecordsGreaterThanLimit() throws ConverterException {
            Integer expected = 1;
            PowerMockito.mockStatic(PaginationHelper.class);
            PowerMockito.mockStatic(DateTimeConverters.class);
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(DateTimeConverters.isoTocal(any(String.class))).thenReturn(Calendar.getInstance());
            when(PaginationHelper.createLink(any(String.class), any(String.class), anyBoolean())).thenReturn(link);
            when(PaginationHelper.determinePageLimit(ArgumentMatchers.eq(limit))).thenReturn(1);
            when(PaginationHelper.determinePageOffset(ArgumentMatchers.eq(offset))).thenReturn(1);
            doReturn(accountUsagesList).when(accountUsageRepository).getAccountUsageRecords(ArgumentMatchers.any(),
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
            Response response = accountsResource.retrieveAllAccountUsage(startTimeParam, endTimeParam, offset, limit);
            Assert.assertEquals(200, response.getStatus());
            Integer actual = ((AccountUsageRecords)response.getEntity()).getAccountUsageRecords().size();
            Assert.assertEquals(expected, actual);
        }
    }

    public static class RetrieveAccountBilling {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private AccountUsageRepository accountUsageRepository;
        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @InjectMocks
        private AccountsResource accountsResource;
        private AccountBilling accountBilling1;
        private AccountBilling accountBilling2;
        private List<AccountBilling> accountBillingList;
        private AccountUsage accountUsage1;
        private AccountUsage accountUsage2;
        private List<AccountUsage> accountUsagesList;
        private String startTimeString;
        private String endTimeString;



        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            accountsResource = new AccountsResource();
            accountsResource.setMockitoAuth(true);
            accountsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            accountsResource.setAccountUsageRepository(accountUsageRepository);
            accountsResource.setLoadBalancerRepository(loadBalancerRepository);

            accountUsage1 = new AccountUsage();
            accountUsage2 = new AccountUsage();
            accountUsage1.setAccountId(8654);
            accountUsage1.setId(1);
            accountUsage2.setAccountId(7654);
            accountUsage2.setId(2);
            accountUsagesList = new ArrayList<AccountUsage>();
            accountUsagesList.add(accountUsage1);
            accountUsagesList.add(accountUsage2);

            accountBilling1 = new AccountBilling();
            accountBilling2 = new AccountBilling();
            accountBilling1.setAccountId(8654);
            accountBilling1.setAccountUsageRecords(accountUsagesList);
            accountBilling2.setAccountId(7654);
            accountBilling2.setAccountUsageRecords(accountUsagesList);
            accountBillingList = new ArrayList<AccountBilling>();
            accountBillingList.add(accountBilling1);
            accountBillingList.add(accountBilling2);

        }

        @Test
        public void shouldThrowBadRequestException400WhenStartTimeOrEndTimeIsNull(){
            startTimeString = null;
            endTimeString = null;
            String expected = "Must provide startTime and endTime query parameters";
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            Assert.assertEquals(400, response.getStatus());
            String actual = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldThrowBadRequestExceptionWhenTimeDiffIsGreaterThanOneDay() throws ConverterException {
            startTimeString = "2021-05-04T13:00:00-05:00";
            endTimeString = "2021-05-05T14:00:00-05:00";
            String actual = "Time range cannot be greater than one day.";
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            String expected = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);

        }

        @Test
        public void shouldThrowBadRequestExceptionWhenTimeDiffIsLessThanZero() throws ConverterException {
            startTimeString = "2021-05-05T14:00:00-05:00";
            endTimeString = "2021-05-04T13:00:00-05:00";
            String actual = "Must specify an earlier startTime than endTime.";
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            String expected = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);

        }

        @Test
        public void shouldThrowConverterException() {
            startTimeString = "2021-05-0514:00:00-05:00";
            endTimeString = "2021-05-0413:00:00-05:00";
            String actual = "Date parameter(s) must follow ISO-8601 format.";
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            String expected = ((GeneralFault)response.getEntity()).getMessage();
            Assert.assertEquals(expected, actual);
        }

        @Test
        public void shouldThrowEntityNotFoundException() {
            startTimeString = "2021-05-05T13:00:00-05:00";
            endTimeString = "2021-05-05T14:00:00-05:00";
            doThrow(EntityNotFoundException.class).when(loadBalancerRepository).getAccountBillingForAllAccounts(ArgumentMatchers.<Calendar>any(), ArgumentMatchers.<Calendar>any());
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            Assert.assertEquals("Object not Found", ((LbaasFault)response.getEntity()).getMessage());
        }

        @Test
        public void shouldThrowReturn200StatusAndRetrieveAllBilling() {
            Integer expected = 2;
            startTimeString = "2021-05-05T13:00:00-05:00";
            endTimeString = "2021-05-05T14:00:00-05:00";
            doReturn(accountBillingList).when(loadBalancerRepository).getAccountBillingForAllAccounts(ArgumentMatchers.<Calendar>any(), ArgumentMatchers.<Calendar>any());
            Response response = accountsResource.retrieveAccountBilling(startTimeString, endTimeString);
            Integer actual = ((AccountBillings)response.getEntity()).getAccountBillings().size();
            Assert.assertEquals(expected, actual);
        }
    }
}
