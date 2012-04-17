package org.openstack.atlas.service.domain.events.entities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

public class SslModeTest {

    private BitTags bitTags;

    @Before
    public void standUp() {
        bitTags = new BitTags();
    }

    @Test
    public void shouldReturnOnWhenOnlySslTagEnabled() {
        bitTags.flipTagOn(BitTag.SSL);
        final SslMode mode = SslMode.getMode(bitTags);
        Assert.assertEquals(SslMode.ON, mode);
    }

    @Test
    public void shouldReturnMixedWhenSslAndSslMixedTagsAreEnabled() {
        bitTags.flipTagOn(BitTag.SSL);
        bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
        final SslMode mode = SslMode.getMode(bitTags);
        Assert.assertEquals(SslMode.MIXED, mode);
    }

    @Test
    public void shouldReturnOffWhenSslAndSslMixedTagsAreDisabled() {
        final SslMode mode = SslMode.getMode(bitTags);
        Assert.assertEquals(SslMode.OFF, mode);
    }

    @Test
    public void shouldReturnOffWhenNonSslTagsAreEnabled() {
        bitTags.flipTagOn(BitTag.SERVICENET_LB);
        bitTags.flipTagOn(BitTag.BIT_TAG_RESERVED_2);
        final SslMode mode = SslMode.getMode(bitTags);
        Assert.assertEquals(SslMode.OFF, mode);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionWhenSslMixedTagIsEnabledWhileSslTagIsNot() {
        bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
        SslMode.getMode(bitTags);
    }
}
