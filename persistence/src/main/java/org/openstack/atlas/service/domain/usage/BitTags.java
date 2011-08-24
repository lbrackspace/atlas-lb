package org.openstack.atlas.service.domain.usage;

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

    public int getBitTags() {
        return bitTags;
    }
}
