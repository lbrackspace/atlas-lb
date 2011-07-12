package org.openstack.atlas.api.helpers;

import org.openstack.atlas.api.exceptions.MalformedUrlException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(Enclosed.class)
public class UrlAccountIdExtractorTest {
    public static class WhenInterrogatingUrlsForExtractingAccountIds {

        @Test
        public void Should_successfully_grab_accountid_from_a_good_url() {
            String url = "http://docs.rackspace.api.com/something/7645/loadbalancers/54";
            Assert.assertEquals(new Integer(7645), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/loadbalancers";
            Assert.assertEquals(new Integer(77777), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/76344/loadbalancers/";
            Assert.assertEquals(new Integer(76344), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/76344/loadbalancers.json";
            Assert.assertEquals(new Integer(76344), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/76344/loadbalancers/9999/accesslist.json";
            Assert.assertEquals(new Integer(76344), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/management/clusters";
            Assert.assertEquals(new Integer(77777), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/";
            Assert.assertEquals(new Integer(77777), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777";
            Assert.assertEquals(new Integer(77777), new UrlAccountIdExtractor().getAccountId(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/43123123";
            Assert.assertEquals(new Integer(77777), new UrlAccountIdExtractor().getAccountId(url));
        }

        @Test(expected = MalformedUrlException.class)
        public void should_throw_malformed_url_exception_when_accountid_not_found() {
            String url = "http://docs.rackspace.api.com/something/loadbalancers/7676";
            new UrlAccountIdExtractor().getAccountId(url);
        }

        @Test(expected = MalformedUrlException.class)
        public void should_throw_malformed_url_exception_when_url_is_null() {
            String url = null;
            new UrlAccountIdExtractor().getAccountId(url);
        }
    }

    public static class WhenInterrogatingUrlsForExtractingContentType {

        @Test
        public void Should_successfully_grab_content_type_from_a_good_url() {
            String url = "http://docs.rackspace.api.com/something/7645/loadbalancers/54.xml";
            Assert.assertEquals("xml", new UrlAccountIdExtractor().getContentType(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/loadbalancers.json";
            Assert.assertEquals("json", new UrlAccountIdExtractor().getContentType(url));
            url = "http://glassfish-test.sitesv2.org:8080/lb-rest/77777/loadbalancers";
            Assert.assertEquals("", new UrlAccountIdExtractor().getContentType(url));
            url = "http://omg.xml.json.org:8080/lb-rest/77777/loadbalancers.xml";
            Assert.assertEquals("xml", new UrlAccountIdExtractor().getContentType(url));
            url = "http://omg.xml.json.org:8080/lb-rest/77777/loadbalancers";
            Assert.assertEquals("", new UrlAccountIdExtractor().getContentType(url));
        }
    }

    public static class WhenTestingRateLimitCaptureGroups {

        @Test
        public void should_successfully_grab_account_id_as_group_one() {
            String url = "http://localhost:9090/lb-rest-service/528830/loadbalancers/196";

            Matcher matcher = Pattern.compile(".*/([0-9]+)/loadbalancers/.*").matcher(url);
            if (matcher.find()) {
                Assert.assertEquals("528830", matcher.group(1));
            }
        }
    }
}
