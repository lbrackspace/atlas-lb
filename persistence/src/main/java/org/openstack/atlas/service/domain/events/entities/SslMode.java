package org.openstack.atlas.service.domain.events.entities;

import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;

import java.io.Serializable;

public enum SslMode implements Serializable {
    ON,
    MIXED,
    OFF;

    private final static long serialVersionUID = 532512316L;

    public static SslMode getMode(BitTags bitTags) {
        if (bitTags.isTagOn(BitTag.SSL) && bitTags.isTagOn(BitTag.SSL_MIXED_MODE)) return MIXED;
        else if (bitTags.isTagOn(BitTag.SSL) && !bitTags.isTagOn(BitTag.SSL_MIXED_MODE)) return ON;
        else if (!bitTags.isTagOn(BitTag.SSL) && !bitTags.isTagOn(BitTag.SSL_MIXED_MODE)) return OFF;
        else throw new RuntimeException(String.format("'%s' tag cannot be enabled when '%s' tag is disabled!", BitTag.SSL_MIXED_MODE.name(), BitTag.SSL.name()));
    }
}
