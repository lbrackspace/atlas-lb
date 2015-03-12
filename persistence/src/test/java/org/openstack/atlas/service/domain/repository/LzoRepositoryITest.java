package org.openstack.atlas.service.domain.repository;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.util.common.CloudFilesSegment;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.crypto.HashUtil;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

import static org.openstack.atlas.service.domain.entities.HdfsLzo.NEEDS_REUPLOAD;
import static org.openstack.atlas.service.domain.entities.HdfsLzo.NEEDS_MD5;
import static org.openstack.atlas.service.domain.entities.HdfsLzo.NEEDS_CF;
import static org.openstack.atlas.service.domain.entities.HdfsLzo.NEEDS_HDFS;
import static org.openstack.atlas.service.domain.entities.HdfsLzo.NEEDS_MASK;

@RunWith(Enclosed.class)
public class LzoRepositoryITest {

    @RunWith(SpringJUnit4ClassRunner.class)
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

            val = lzoService.newHdfsLzo(hourKey);
            Assert.assertEquals(-1, val);
            val = lzoService.newHdfsLzo(hourKey);
            Assert.assertEquals(val, NEEDS_CF | NEEDS_HDFS | NEEDS_MD5);
            Assert.assertEquals(NEEDS_MD5 | NEEDS_CF | NEEDS_HDFS, lzoService.getStateFlags(hourKey));
            val = lzoService.newHdfsLzo(hourKey);
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
            lzoService.setStateFlagsTrue(hourKey,NEEDS_REUPLOAD);
            Assert.assertEquals(-1, lzoService.newHdfsLzo(hourKey));
            Assert.assertEquals(NEEDS_CF|NEEDS_MD5, lzoService.newHdfsLzo(hourKey));
        }
    }
}
