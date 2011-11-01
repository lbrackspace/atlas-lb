package org.opestack.atlas.api.mapper.dozer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.stub.StubFactory;

@RunWith(Enclosed.class)
public class NodeMappingTest {

    public static class WhenMappingANodeFromApiToDomain extends MappingBase {
        private org.openstack.atlas.core.api.v1.Node apiNode;
        private org.openstack.atlas.service.domain.entity.Node domainNode;

        @Before
        public void setUp() throws Exception {
            apiNode = StubFactory.createHydratedDataModelNode();
            domainNode = mapper.map(apiNode, org.openstack.atlas.service.domain.entity.Node.class);
        }

        @Test
        public void shouldNotFailWhenApiNodeIsEmpty() {
            apiNode = new org.openstack.atlas.core.api.v1.Node();
            try {
                domainNode = mapper.map(apiNode, org.openstack.atlas.service.domain.entity.Node.class);
            } catch (Exception e) {
                Assert.fail("Empty API node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(apiNode.getId(), domainNode.getId());
            Assert.assertEquals(apiNode.getAddress(), domainNode.getAddress());
            Assert.assertEquals(apiNode.getPort(), domainNode.getPort());
            Assert.assertEquals(apiNode.getWeight(), domainNode.getWeight());
            Assert.assertEquals(apiNode.getStatus(), domainNode.getStatus());
        }
    }


    public static class WhenMappingANodeFromDomainToApi extends MappingBase {
        private org.openstack.atlas.service.domain.entity.Node domainNode;
        private org.openstack.atlas.core.api.v1.Node apiNode;

        @Before
        public void setUp() throws Exception {
            domainNode = StubFactory.createHydratedDomainNode();
            apiNode = mapper.map(domainNode, org.openstack.atlas.core.api.v1.Node.class);
        }

        @Test
        public void shouldNotFailWhenDomainNodeIsEmpty() {
            domainNode = new org.openstack.atlas.service.domain.entity.Node();
            try {
                apiNode = mapper.map(domainNode, org.openstack.atlas.core.api.v1.Node.class);
            } catch (Exception e) {
                Assert.fail("Empty domain node caused this exception");
            }
        }

        @Test
        public void shouldMapAllAttributes() {
            Assert.assertEquals(domainNode.getId(), apiNode.getId());
            Assert.assertEquals(domainNode.getAddress(), apiNode.getAddress());
            Assert.assertEquals(domainNode.getPort(), apiNode.getPort());
            Assert.assertEquals(domainNode.getWeight(), apiNode.getWeight());
            Assert.assertEquals(domainNode.getStatus(), apiNode.getStatus());
        }

        @Test
        public void shouldNotMapExtensionAttributes() {
            Assert.assertTrue(apiNode.getOtherAttributes().isEmpty());
        }
    }
}

