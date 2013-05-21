package org.openstack.atlas.service.domain.pojos;

public class LbIdAccountId {
    private Integer lbId;
    private Integer accountId;

    public LbIdAccountId(Integer lbId, Integer accountId) {
        this.lbId = lbId;
        this.accountId = accountId;
    }

    public Integer getLbId() {
        return lbId;
    }

    public void setLbId(Integer lbId) {
        this.lbId = lbId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LbIdAccountId that = (LbIdAccountId) o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (lbId != null ? !lbId.equals(that.lbId) : that.lbId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lbId != null ? lbId.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }
}
