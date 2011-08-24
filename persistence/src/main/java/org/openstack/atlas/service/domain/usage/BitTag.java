package org.openstack.atlas.service.domain.usage;

public enum BitTag {
    SSL(1),
    SERVICENET_LB(2),
    
    /* Reserved bits for future use. We should
     * have a total of 32 bit flags. */
    BIT_TAG_RESERVED_1(4),
    BIT_TAG_RESERVED_2(8);

    private final int tagValue;

    BitTag(int tagValue) {
        this.tagValue = tagValue;
    }

    public int tagValue() {
        return tagValue;
    }
}
