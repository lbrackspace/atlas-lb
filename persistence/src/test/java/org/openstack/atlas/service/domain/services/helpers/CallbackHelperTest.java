package org.openstack.atlas.service.domain.services.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;
import org.openstack.atlas.service.domain.services.impl.CallbackServiceImpl;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;
import org.openstack.atlas.service.domain.services.impl.NotificationServiceImpl;

@RunWith(Enclosed.class)
public class CallbackHelperTest {
    public static class handleZeusEvent {
        CallbackServiceImpl callbackService;
        NodeServiceImpl nodeService;
        NotificationServiceImpl notificationService;
        ZeusEvent zEvent;
        CallbackHelper callbackHelper;

        String mFail = "WARN monitors/12345_62203 monitorfail Monitor has detected a failure in node '10.1.223.134:443': Invalid HTTP response received; premature end of headers";
        String mFail6 = "WARN monitors/386085_3034 monitorfail Monitor has detected a failure in node '[2001:4801:79f1:1:22d6:5749:0:3a]:80 (2001:4801:79f1:1:22d6:5749::3a)': Connect failed: Connection refused";
        String mOK = "INFO monitors/12345_62203 monitorok Monitor is working for node '10.1.223.134:443'.";

        @Before
        public void standUp() {
            try {
                callbackHelper = new CallbackHelper(mFail);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }

        }

        @Test
        public void shouldSplitAccountID() throws Exception, BadRequestException {
            Integer aid = callbackHelper.accountId;
            Assert.assertEquals((Integer) 12345, aid);
        }

        @Test
        public void shouldSplitLBID() throws Exception {
            Integer lbid = callbackHelper.loadBalancerId;
            Assert.assertEquals((Integer) 62203, lbid);
        }

        @Test
        public void shouldSplitDetailedMessage() throws Exception {
            String dMsg = callbackHelper.detailedMessage;
            Assert.assertEquals("Invalid HTTP response received; premature end of headers", dMsg);
        }

        @Test
        public void shouldSplitDetailedMessageForIPv6() throws Exception {
            callbackHelper = new CallbackHelper(mFail6);
            String dMsg = callbackHelper.detailedMessage;
            Assert.assertEquals("Connect failed: Connection refused", dMsg);
        }

        @Test
        public void shouldNotSplitDetailedMessageForOk() throws Exception {
            callbackHelper = new CallbackHelper(mOK);
            String dMsg = callbackHelper.detailedMessage;
            Assert.assertEquals("", dMsg);
        }

        @Test
        public void shouldSplitPort() throws Exception {
            Integer port = callbackHelper.port;
            Assert.assertEquals((Integer) 443, port);
        }

        @Test
        public void shouldSplitPortForOK() throws Exception {
            callbackHelper = new CallbackHelper(mOK);
            Integer port = callbackHelper.port;
            Assert.assertEquals((Integer) 443, port);
        }

        @Test
        public void shouldSplitPortForIPV6() throws Exception {
            callbackHelper = new CallbackHelper(mFail6);
            Integer port = callbackHelper.port;
            Assert.assertEquals((Integer) 80, port);
        }

        @Test
        public void shouldSplitAddress() throws Exception {
            String address = callbackHelper.ipAddress;
            Assert.assertEquals("10.1.223.134", address);
        }

        @Test
        public void shouldSplitAddressForOK() throws Exception {
            callbackHelper = new CallbackHelper(mOK);
            String address = callbackHelper.ipAddress;
            Assert.assertEquals("10.1.223.134", address);
        }

        @Test
        public void shouldSplitAddressForIPV6() throws Exception {
            callbackHelper = new CallbackHelper(mFail6);
            String address = callbackHelper.ipAddress;
            Assert.assertEquals("2001:4801:79f1:1:22d6:5749:0:3a", address);
        }

        @Test(expected = Exception.class)
        public void shouldFailForBadParamLine() throws Exception {
            callbackHelper = new CallbackHelper("bad param line");
        }

        @Test
        public void shouldReturnNodeWorkingTag() throws EntityNotFoundException, BadRequestException {
            Assert.assertEquals("nodeworking", CallbackHelper.NODE_WORKING_TAG);
        }

        @Test
        public void shouldReturnNodeFailTag() throws EntityNotFoundException, BadRequestException {
            Assert.assertEquals("nodefail", CallbackHelper.NODE_FAIL_TAG);
        }

        @Test
        public void shouldReturnMonitorWorkingTag() throws EntityNotFoundException, BadRequestException {
            Assert.assertEquals("monitorok", CallbackHelper.MONITOR_WORKING_TAG);
        }

        @Test
        public void shouldReturnMonitorFailTag() throws EntityNotFoundException, BadRequestException {
            Assert.assertEquals("monitorfail", CallbackHelper.MONITOR_FAIL_TAG);
        }
    }
}
