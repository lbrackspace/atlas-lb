package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.repository.GroupRepository;
import org.openstack.atlas.service.domain.services.impl.GroupServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.Assert;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class GroupServiceImplTest {

    public static class updateGroupDefaults {
        Integer accountId = 1234;
        GroupRepository groupRepository;
        GroupServiceImpl groupService;
        GroupRateLimit groupRateLimit1;
        GroupRateLimit groupRateLimit2;
        GroupRateLimit groupRateLimit3;
        List<GroupRateLimit> groupRateLimitList;

        @Before
        public void standUp() {
            groupRepository = mock(GroupRepository.class);
            groupService = new GroupServiceImpl();
            groupService.setGroupRepository(groupRepository);

        }

        @Before
        public void setUpObjects() {
            groupRateLimit1 = new GroupRateLimit();
            groupRateLimit2 = new GroupRateLimit();
            groupRateLimit3 = new GroupRateLimit();
            groupRateLimitList = new ArrayList<GroupRateLimit>();

            groupRateLimit1.setDefault(true);
            groupRateLimit1.setId(1);
            groupRateLimit1.setDescription("description");
            groupRateLimit1.setName("aName");

            groupRateLimit3.setDefault(true);
            groupRateLimit3.setId(3);
            groupRateLimit3.setDescription("description");
            groupRateLimit3.setName("aName");

            groupRateLimitList.add(groupRateLimit1);
            groupRateLimitList.add(groupRateLimit3);
        }

        @Test
        public void shouldSetOthersNum1NonDefaultIfUserSetsAnotherDefault() {
            groupRateLimit2.setDefault(true);
            groupRateLimit2.setId(2);
            when(groupRepository.getAll()).thenReturn(groupRateLimitList);
            groupService.updateGroupDefaults(groupRateLimit2);
            Assert.assertFalse(groupRateLimit1.getDefault());
        }

        @Test
        public void shouldSetOthersNum3NonDefaultIfUserSetsAnotherDefault() {
            groupRateLimit2.setDefault(true);
            groupRateLimit2.setId(2);
            when(groupRepository.getAll()).thenReturn(groupRateLimitList);
            groupService.updateGroupDefaults(groupRateLimit2);
            Assert.assertFalse(groupRateLimit3.getDefault());
        }

        @Test
        public void shouldSetDefaultIfUserSetsAsDefault() {
            groupRateLimit2.setDefault(true);
            groupRateLimit2.setId(2);
            when(groupRepository.getAll()).thenReturn(groupRateLimitList);
            groupService.updateGroupDefaults(groupRateLimit2);
            Assert.assertTrue(groupRateLimit2.getDefault());
        }
    }
}
