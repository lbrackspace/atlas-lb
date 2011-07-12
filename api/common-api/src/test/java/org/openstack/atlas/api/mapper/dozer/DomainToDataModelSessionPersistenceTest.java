package org.openstack.atlas.api.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class DomainToDataModelSessionPersistenceTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_session_persistence_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private SessionPersistence sessionPersistence;
        private org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence dataModelSessionPersistence;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            sessionPersistence = SessionPersistence.HTTP_COOKIE;
        }

        @Test
        public void shouldMapToNullWhenSessionPersistenceTypeIsNONE() {
            sessionPersistence = SessionPersistence.NONE;
            try {
                dataModelSessionPersistence = mapper.map(sessionPersistence, org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence.class);
            } catch (Exception e) {
                Assert.fail("Exception caused by session persistence type being null");
            }
        }

        @Test
        public void shouldMapToHttpCookie() {
            dataModelSessionPersistence = mapper.map(sessionPersistence, org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence.class);
            Assert.assertEquals(PersistenceType.HTTP_COOKIE, dataModelSessionPersistence.getPersistenceType());
        }
    }
}
