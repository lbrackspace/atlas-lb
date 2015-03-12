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
import org.openstack.atlas.service.domain.services.helpers.LzoState;
import org.openstack.atlas.util.common.CloudFilesSegment;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.crypto.HashUtil;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

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
        public void testCreateLzoPairs() throws UnsupportedEncodingException, NoSuchAlgorithmException {
            int hour_key = 2015010112;
            int nFrags = 6;
            int i;
            CloudFilesSegmentContainer cnt = new CloudFilesSegmentContainer();
            List<CloudFilesSegment> segs = cnt.getSegments();
            String hourKeyStr = StaticStringUtils.lpadLong(hour_key, "0", 10);
            String fileName = String.format("%s-access_log.aggregated.lzo", hourKeyStr);
            for (i = 0; i < nFrags; i++) {
                CloudFilesSegment seg = new CloudFilesSegment();
                seg.setFileName(fileName);
                seg.setMd5sum(String.format("%d:%d", hour_key, i));
                seg.setFragNumber(i);
                segs.add(seg);
                Assert.assertTrue(lzoService.getCloudFilesLzoState(hour_key, nFrags) == LzoState.EMPTY_ENTRY);
            }
            Assert.assertTrue(lzoService.getHdfsLzoState(hour_key) == LzoState.EMPTY_ENTRY);
            lzoService.createLzoPairs(hour_key, cnt);

            List<CloudFilesLzo> cloudFilesLzo = lzoService.getCloudFilesLzo(hour_key);
            Assert.assertTrue(cloudFilesLzo.size() == nFrags);
            for (i = 0; i < nFrags; i++) {
                Assert.assertTrue(lzoService.getCloudFilesLzoState(hour_key, i) == LzoState.SENDING);
            }
            Assert.assertFalse(lzoService.isLzoFinished(hour_key));
            for (i = 0; i < nFrags; i++) {
                Assert.assertFalse(lzoService.isLzoFinished(hour_key));
                lzoService.setFinishedCloudFilesLzo(hour_key, i);
            }
            Assert.assertFalse(lzoService.isLzoFinished(hour_key));
            lzoService.setFinishedHdfsLzo(hour_key);
            Assert.assertTrue(lzoService.isLzoFinished(hour_key));
        }
    }
}
