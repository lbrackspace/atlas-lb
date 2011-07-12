package org.openstack.atlas.api.mapper.dozer;

import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.dozer.DozerBeanMapper;
import org.dozer.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class DataModelToDomainSessionPersistenceTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    public static class When_mapping_session_persistence_from_datamodel_to_domain {
        private DozerBeanMapper mapper;
        private SessionPersistence sessionPersistence;
        private org.openstack.atlas.service.domain.entities.SessionPersistence domainSessionPersistence;


        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            sessionPersistence = new SessionPersistence();
            sessionPersistence.setPersistenceType(PersistenceType.HTTP_COOKIE);
        }

        @Test
        public void shouldMapToNoneWhenSessionPersistenceTypeIsNotSet() {
            sessionPersistence = new SessionPersistence();
            try {
                domainSessionPersistence = mapper.map(sessionPersistence, org.openstack.atlas.service.domain.entities.SessionPersistence.class);
            }
            catch (Exception e) {
                Assert.fail("Exception caused by session persistence type being null");
            }

            Assert.assertEquals(org.openstack.atlas.service.domain.entities.SessionPersistence.NONE, domainSessionPersistence);
        }

        @Test
        public void shouldMapToHttpCookie() {
            domainSessionPersistence = mapper.map(sessionPersistence, org.openstack.atlas.service.domain.entities.SessionPersistence.class);
            Assert.assertEquals(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE, domainSessionPersistence);
        }
    }

    public static class When_mapping_session_persistence_type_from_datamodel_to_domain {
        private DozerBeanMapper mapper;
        private PersistenceType persistenceType;
        private org.openstack.atlas.service.domain.entities.SessionPersistence domainSessionPersistence;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            persistenceType = PersistenceType.HTTP_COOKIE;
        }

        @Test(expected = MappingException.class)
        public void shouldThrowDozerMappingExceptionWhenPersistenceTypeIsNull() {
            persistenceType = null;
            domainSessionPersistence = mapper.map(persistenceType, org.openstack.atlas.service.domain.entities.SessionPersistence.class);
        }

        @Test
        public void shouldMapToHttpCookie() {
            domainSessionPersistence = mapper.map(persistenceType, org.openstack.atlas.service.domain.entities.SessionPersistence.class);
            Assert.assertEquals(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE, domainSessionPersistence);
        }
    }
}
