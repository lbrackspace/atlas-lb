package org.openstack.atlas.service.domain.entities;

import java.io.Serializable;
import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "account")
public class Account implements Serializable {

    private final static long serialVersionUID = 532512316L;
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;
    @Column(name = "sha1sum_ipv6", unique = true, nullable = false)
    private String sha1SumForIpv6;
    @Enumerated(EnumType.STRING)
    @Column(name = "cluster_type", length = 32)
    private ClusterType clusterType = ClusterType.STANDARD;

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

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{id=").append((id == null) ? "NULL" : id.toString()).
                append(", sha1=").append((sha1SumForIpv6 == null) ? "NULL" : sha1SumForIpv6).
                append(", clusterType=").append((clusterType == null) ? "NULL" : clusterType.toString()).
                append("}");
        return sb.toString();
    }
}
