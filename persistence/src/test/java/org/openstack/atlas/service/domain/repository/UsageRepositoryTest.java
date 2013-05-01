package org.openstack.atlas.service.domain.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:dbunit-loadbalancing-context.xml"})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
public class UsageRepositoryTest {
    @Autowired
    private UsageRepository usageRepository;
    private int accountId = 1234;
    private int lbId = 1234;

    @Ignore
    @Test
    @DatabaseSetup("classpath:org/openstack/atlas/service/domain/repository/case001.xml")
    public void shouldReturnMostRecentRecordWhenOnlyOneRecentRecord() throws EntityNotFoundException {
        Usage mostRecentUsageForLoadBalancer = usageRepository.getMostRecentUsageForLoadBalancer(lbId);

        Assert.assertEquals(Integer.valueOf(1), mostRecentUsageForLoadBalancer.getId());
    }
}
