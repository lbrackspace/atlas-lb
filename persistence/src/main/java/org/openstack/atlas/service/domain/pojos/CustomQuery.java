package org.openstack.atlas.service.domain.pojos;

import java.util.ArrayList;
import java.util.List;

public class CustomQuery {

    private static final int strInitSize = 256;
    private List<QueryParameter> queryParameters;
    private List<QueryParameter> unquotedParameters;
    private String selectClause;
    private Integer limit;
    private Integer offset;
    private String WherePrefix = " WHERE ";

    public CustomQuery() {
    }

    public CustomQuery(String selectClause) {
        queryParameters = new ArrayList<QueryParameter>();
        this.selectClause = selectClause;
    }

    public void clearParameters() {
        queryParameters = new ArrayList<QueryParameter>();
    }

    public void clearUnquotedParameters() {
        unquotedParameters = new ArrayList<QueryParameter>();
    }

    public void addUnquotedParam(String pname, Object val) {
        if (unquotedParameters == null) {
            clearUnquotedParameters();
        }
        QueryParameter param = new QueryParameter();
        param.setQname(null);
        param.setOp(null);
        param.setPname(pname);
        param.setValue(val);
        unquotedParameters.add(param);
    }

    public void addParam(String qname, String op, String pname, Object val) {
        if (queryParameters == null) {
            clearParameters();
        }
        QueryParameter param = new QueryParameter();
        param.setQname(qname);
        param.setOp(op);
        param.setPname(pname);
        param.setValue(val);
        queryParameters.add(param);
    }

    public String getQueryString() {
        String frm;
        String qStr;
        String out;
        int i;
        StringBuilder where = new StringBuilder(strInitSize);
        QueryParameter param;
        if (queryParameters == null || queryParameters.size() < 1) {
            return selectClause;
        }

        where.append(WherePrefix);
        for (i = 0; i < queryParameters.size(); i++) {
            param = queryParameters.get(i);
            qStr = String.format("%s %s :%s", param.getQname(), param.getOp(), param.getPname());
            if (i < queryParameters.size() - 1) {
                qStr += " and ";
            }
            where.append(qStr);
        }
        out = String.format("%s%s", selectClause, where.toString());
        return out;
    }

    public List<QueryParameter> getQueryParameters() {
        if(queryParameters == null) {
            clearParameters();
        }
        return queryParameters;
    }

    public void setQueryParameters(List<QueryParameter> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public List<QueryParameter> getUnquotedParameters() {
        if(unquotedParameters == null) {
            clearUnquotedParameters();
        }
        return unquotedParameters;
    }

    public void setUnquotedParameters(List<QueryParameter> unquotedParameters) {
        this.unquotedParameters = unquotedParameters;
    }

    public String getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    @Override
    public String toString() {
        String out = getQueryString();
        return out;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getWherePrefix() {
        return WherePrefix;
    }

    public void setWherePrefix(String WherePrefix) {
        this.WherePrefix = WherePrefix;
    }
}
