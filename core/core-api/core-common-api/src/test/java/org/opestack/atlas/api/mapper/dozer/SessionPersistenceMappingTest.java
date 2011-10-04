package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class SessionPersistenceMappingTest {

    public static class WhenMappingFromApiToDomain extends MappingBase {
        private org.openstack.atlas.core.api.v1.SessionPersistence apiSessionPersistence;
        private org.openstack.atlas.service.domain.entity.SessionPersistence domainSessionPersistence;

        @Before
        public void setUp() throws Exception {
            apiSessionPersistence = StubFactory.createHydratedDataModelSessionPersistence();
            domainSessionPersistence = mapper.map(apiSessionPersistence, org.openstack.atlas.service.domain.entity.SessionPersistence.class);
        }

        @Test
        public void shouldNotFailWhenApiPersistenceIsSetToNone() {
            apiSessionPersistence = new org.openstack.atlas.core.api.v1.SessionPersistence();
            apiSessionPersistence.setPersistenceType("NONE");
            try {
                domainSessionPersistence = mapper.map(apiSessionPersistence, org.openstack.atlas.service.domain.entity.SessionPersistence.class);
            } catch (Exception e) {
                Assert.fail("Empty API node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiSessionPersistence.getPersistenceType(), domainSessionPersistence.name());
        }

        @Test
        public void shouldMapAttributesToNullWhenApiPersistenceIsSetToNone() {
            apiSessionPersistence = new SessionPersistence();
            apiSessionPersistence.setPersistenceType("NONE");
            domainSessionPersistence = mapper.map(apiSessionPersistence, org.openstack.atlas.service.domain.entity.SessionPersistence.class);

            Assert.assertEquals(domainSessionPersistence.name(), apiSessionPersistence.getPersistenceType());
        }
    }

    public static class WhenMappingFromDomainToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.SessionPersistence domainSessionPersistence;
        private org.openstack.atlas.core.api.v1.SessionPersistence apiSessionPersistence;

        @Before
        public void setUp() throws Exception {
            domainSessionPersistence = org.openstack.atlas.service.domain.entity.SessionPersistence.HTTP_COOKIE;
            apiSessionPersistence = mapper.map(domainSessionPersistence, org.openstack.atlas.core.api.v1.SessionPersistence.class);
        }

        @Test
        public void shouldNotFailWhenDomainPersistenceIsNull() {
            domainSessionPersistence = org.openstack.atlas.service.domain.entity.SessionPersistence.NONE;
            try {
                apiSessionPersistence = mapper.map(domainSessionPersistence, org.openstack.atlas.core.api.v1.SessionPersistence.class);
            } catch (Exception e) {
                Assert.fail("Empty API node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainSessionPersistence.name(), apiSessionPersistence.getPersistenceType());
        }

        @Test
        public void shouldMapAttributesToNullWhenNoAttributesSet() {
            domainSessionPersistence = org.openstack.atlas.service.domain.entity.SessionPersistence.NONE;
            apiSessionPersistence = mapper.map(domainSessionPersistence, org.openstack.atlas.core.api.v1.SessionPersistence.class);

            Assert.assertNull(apiSessionPersistence);
        }
    }
}

