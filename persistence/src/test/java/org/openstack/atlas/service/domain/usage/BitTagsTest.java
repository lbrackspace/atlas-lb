package org.openstack.atlas.service.domain.usage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class BitTagsTest {

    private static final int sslOnly = BitTag.SSL.tagValue();
    private static final int sslMixed = BitTag.SSL_MIXED_MODE.tagValue();
    private static final int servicenetOnly = BitTag.SERVICENET_LB.tagValue();
    private static final int sslAndServicenet = sslOnly + servicenetOnly;

    public static class WhenPassingInBitTagsThroughConstructor {

        @Test
        public void shouldHaveCorrectTagsSetWhenOnlySslTagIsSet() {
            BitTags bitTags = new BitTags(sslOnly);

            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertFalse(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }

        @Test
        public void shouldHaveCorrectTagsSetWhenOnlyServiceNetTagIsSet() {
            BitTags bitTags = new BitTags(servicenetOnly);

            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }

        @Test
        public void shouldHaveCorrectTagsSetWhenSslAndServiceNetTagsAreSet() {
            BitTags bitTags = new BitTags(sslAndServicenet);

            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }
    }

    public static class WhenPassingUsingEmptyConstructor {

        @Test
        public void shouldHaveCorrectTagsSetWhenOnlySslTagIsSet() {
            BitTags bitTags = new BitTags();
            bitTags.flipTagOn(BitTag.SSL);

            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertFalse(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }

        @Test
        public void shouldHaveCorrectTagsSetWhenOnlyServicenetTagIsSet() {
            BitTags bitTags = new BitTags();
            bitTags.flipTagOn(BitTag.SERVICENET_LB);

            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }

        @Test
        public void shouldHaveCorrectTagsSetWhenSslAndServiceNetTagsAreSet() {
            BitTags bitTags = new BitTags();
            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SERVICENET_LB);

            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }

        @Test
        public void shouldHaveCorrectTagsWhenFlippingTagsOnAndOff() {
            BitTags bitTags = new BitTags();

            bitTags.flipTagOn(BitTag.SSL);
            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertFalse(bitTags.isTagOn(BitTag.SERVICENET_LB));

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));

            bitTags.flipTagOff(BitTag.SSL);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            Assert.assertTrue(bitTags.isTagOn(BitTag.SERVICENET_LB));

            bitTags.flipTagOff(BitTag.SERVICENET_LB);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            Assert.assertFalse(bitTags.isTagOn(BitTag.SERVICENET_LB));
        }
    }

    public static class WhenFlippingTagsMultipleTimes {

        @Test
        public void shouldHaveNoSslWhenDisablingSslOnlyMultipleTimes() {
            BitTags bitTags = new BitTags(sslOnly);
            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL));
            bitTags.flipTagOff(BitTag.SSL);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            bitTags.flipTagOff(BitTag.SSL);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
            bitTags.flipTagOff(BitTag.SSL);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL));
        }

        @Test
        public void shouldHaveNoSslWhenDisablingSslMixedMultipleTimes() {
            BitTags bitTags = new BitTags(sslMixed);
            Assert.assertTrue(bitTags.isTagOn(BitTag.SSL_MIXED_MODE));
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL_MIXED_MODE));
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL_MIXED_MODE));
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
            Assert.assertFalse(bitTags.isTagOn(BitTag.SSL_MIXED_MODE));
        }
    }
}
