/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstack.atlas.test;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * 
 * @author kalebpomeroy
 */
//public abstract class BaseQuartzTest extends AbstractJpaTests {
public abstract class BaseQuartzTest extends AbstractTransactionalDataSourceSpringContextTests {

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"testQuartzContext.xml"};
    }

}
