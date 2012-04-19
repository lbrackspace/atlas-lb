package org.openstack.atlas.service.domain.usage;

public enum BitTag {
    SSL(1),
    SERVICENET_LB(2),
    SSL_MIXED_MODE(4), // This can only be 'on' if the SSL tag is on
    
    /* Reserved bits for future use. We should
     * have a total of 31 bit flags. */

    /* The int data type is a 32-bit signed two's complement integer.
     * It has a minimum value of -2,147,483,648 and a maximum value
     * of 2,147,483,647 (inclusive). For integral values, this data
     * type is generally the default choice unless there is a reason
     * to choose something else. This data type will most likely be
     * large enough for the numbers your program will use, but if you
     * need a wider range of values, use long instead.*/

    BIT_TAG_RESERVED_2(8),
    BIT_TAG_RESERVED_3(16),
    BIT_TAG_RESERVED_4(32),
    BIT_TAG_RESERVED_5(64),
    BIT_TAG_RESERVED_6(128),
    BIT_TAG_RESERVED_7(256),
    BIT_TAG_RESERVED_8(512),
    BIT_TAG_RESERVED_9(1024),
    BIT_TAG_RESERVED_10(2048),
    BIT_TAG_RESERVED_11(4096),
    BIT_TAG_RESERVED_12(8192),
    BIT_TAG_RESERVED_13(16384),
    BIT_TAG_RESERVED_14(32768),
    BIT_TAG_RESERVED_15(65536),
    BIT_TAG_RESERVED_16(131072),
    BIT_TAG_RESERVED_17(262144),
    BIT_TAG_RESERVED_18(524288),
    BIT_TAG_RESERVED_19(1048576),
    BIT_TAG_RESERVED_20(2097152),
    BIT_TAG_RESERVED_21(4194304),
    BIT_TAG_RESERVED_22(8388608),
    BIT_TAG_RESERVED_23(16777216),
    BIT_TAG_RESERVED_24(33554432),
    BIT_TAG_RESERVED_25(67108864),
    BIT_TAG_RESERVED_26(134217728),
    BIT_TAG_RESERVED_27(268435456),
    BIT_TAG_RESERVED_28(536870912),
    BIT_TAG_RESERVED_29(1073741824);

    private final int tagValue;

    BitTag(int tagValue) {
        this.tagValue = tagValue;
    }

    public int tagValue() {
        return tagValue;
    }
}
