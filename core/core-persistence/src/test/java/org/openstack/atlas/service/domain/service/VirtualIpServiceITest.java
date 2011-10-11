package org.openstack.atlas.service.domain.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(Enclosed.class)
public class VirtualIpServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenAssigningVipsToLoadBalancer extends Base {
        @Autowired
        private VirtualIpService virtualIpService;

        @Autowired
        private VirtualIpRepository virtualIpRepository;

        @Autowired
        VirtualIpv6Repository virtualIpv6Repository;

        @Before
        public void standUp() {

        }
    }
}
