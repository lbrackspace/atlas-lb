package org.openstack.atlas.service.domain.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;

import static org.openstack.atlas.service.domain.entities.HdfsLzo.*;

@RunWith(Enclosed.class)
public class LzoRepositoryITest {

    private static final long sixteenGigs = 16L * 1024L * 1024L * 1024L;

    @RunWith(Enclosed.class)
    public static class WhenTestingWhatever extends Base {

        @Before
        @Override
        public void standUp() throws Exception {
            super.standUp();
        }

        @Test
        public void testLzoCycle() {
            int hourKey = 2015010112;
            int val;

            val = lzoService.newHdfsLzo(hourKey,sixteenGigs);
            Assert.assertEquals(-1, val);
            val = lzoService.newHdfsLzo(hourKey,sixteenGigs);
            Assert.assertEquals(val, NEEDS_CF | NEEDS_HDFS | NEEDS_MD5);
            Assert.assertEquals(NEEDS_MD5 | NEEDS_CF | NEEDS_HDFS, lzoService.getStateFlags(hourKey));
            val = lzoService.newHdfsLzo(hourKey, sixteenGigs);
            lzoService.setStateFlagsFalse(hourKey, NEEDS_HDFS);
            Assert.assertEquals(NEEDS_MD5 | NEEDS_CF, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsFalse(hourKey, NEEDS_MD5);
            Assert.assertEquals(NEEDS_CF, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsFalse(hourKey, NEEDS_CF);
            Assert.assertEquals(0, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsTrue(hourKey, NEEDS_HDFS);
            Assert.assertEquals(NEEDS_HDFS, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsTrue(hourKey, NEEDS_CF);
            Assert.assertEquals(NEEDS_CF | NEEDS_HDFS, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsTrue(hourKey, NEEDS_MD5);
            Assert.assertEquals(NEEDS_CF | NEEDS_HDFS | NEEDS_MD5, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsFalse(hourKey, NEEDS_MASK);
            Assert.assertEquals(0, lzoService.getStateFlags(hourKey));
            lzoService.setStateFlagsTrue(hourKey, NEEDS_REUPLOAD);
            Assert.assertEquals(-1, lzoService.newHdfsLzo(hourKey,sixteenGigs));
            Assert.assertEquals(NEEDS_CF | NEEDS_MD5, lzoService.newHdfsLzo(hourKey,sixteenGigs));
        }
    }
}
