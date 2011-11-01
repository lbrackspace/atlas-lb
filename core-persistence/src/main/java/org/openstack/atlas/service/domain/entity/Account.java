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
@Table(name = "account")
public class Account implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "sha1sum_ipv6", unique = true, nullable = false)
    private String sha1SumForIpv6;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSha1SumForIpv6() {
        return sha1SumForIpv6;
    }

    public void setSha1SumForIpv6(String sha1SumForIpv6) {
        this.sha1SumForIpv6 = sha1SumForIpv6;
    }
}
