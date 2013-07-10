package org.openstack.atlas.service.domain.usage;

import org.openstack.atlas.service.domain.events.UsageEvent;

public final class BitTags {
    private int bitTags = 0;

    public BitTags() {
    }

    public BitTags(int bitTags) {
        this.bitTags = bitTags;
    }

    public void flipTagOn(BitTag bitTag) {
        if (!isTagOn(bitTag)) bitTags += bitTag.tagValue();
    }

    public void flipTagOff(BitTag bitTag) {
        if (isTagOn(bitTag)) bitTags -= bitTag.tagValue();
    }

    public void flipAllTagsOff() {
        bitTags = 0;
    }

    public boolean isTagOn(BitTag bitTag) {
        return BitTags.isTagOn(bitTags, bitTag);
    }

    public static boolean isTagOn(int bitTags, BitTag bitTag) {
        return (bitTag.tagValue() & bitTags) == bitTag.tagValue();
    }

    public int toInt() {
        return bitTags;
    }

    public void applyEvent(UsageEvent usageEvent) {
        switch(usageEvent) {
            case SSL_OFF:
                flipTagOff(BitTag.SSL);
                flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_ONLY_ON:
                flipTagOn(BitTag.SSL);
                flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_MIXED_ON:
                flipTagOn(BitTag.SSL);
                flipTagOn(BitTag.SSL_MIXED_MODE);
                break;
            case DELETE_LOADBALANCER:
                flipAllTagsOff();
            default:
                break;
        }
    }
}
