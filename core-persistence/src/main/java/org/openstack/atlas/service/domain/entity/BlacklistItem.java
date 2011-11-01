package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "blacklist_item")
public class BlacklistItem extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "cidr_block", length = 64, nullable = false)
    private String cidrBlock;

    @Column(name = "ip_version", nullable = false)
    @Enumerated(EnumType.STRING)
    private IpVersion ipVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = true)
    private BlacklistType blacklistType;

    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(IpVersion ipVersion) {
        this.ipVersion = ipVersion;
    }

    public BlacklistType getBlacklistType() {
        return blacklistType;
    }

    public void setBlacklistType(BlacklistType blacklistType) {
        this.blacklistType = blacklistType;
    }
}
