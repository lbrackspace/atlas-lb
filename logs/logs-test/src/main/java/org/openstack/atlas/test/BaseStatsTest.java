package org.openstack.atlas.test;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * 
 * @author kalebpomeroy
 */

// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(locations = {"classpath:testStatsDaosContext.xml" })
// @TestExecutionListeners({DependencyInjectionTestExecutionListener.class })
// @TransactionConfiguration(defaultRollback = false, transactionManager =
// "statsTransactionManager")
//public abstract class BaseStatsTest extends AbstractJpaTests {
public abstract class BaseStatsTest extends AbstractTransactionalDataSourceSpringContextTests {

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"testStatsDaosContext.xml"};
    }

}
