package org.openstack.atlas.service.domain.usage;

public final class BitTags {
    public static final int BIT_TAG_SSL = 1;
    /* Reserved bits for future use. We should
     * have a total of 32 bit flags. */
    public static final int BIT_TAG_RESERVED_1 = 2;
    public static final int BIT_TAG_RESERVED_2 = 4;

    private int bitTags = 0;

    public BitTags() {
    }

    public BitTags(int bitTags) {
        this.bitTags = bitTags;
    }

    public boolean isSsl() {
        return BitTags.isSsl(bitTags);
    }

    public static boolean isSsl(int bitTags){
        return (BIT_TAG_SSL&bitTags)==1;
    }


}
