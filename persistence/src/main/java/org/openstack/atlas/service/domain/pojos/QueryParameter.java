package org.openstack.atlas.service.domain.pojos;

public class QueryParameter {

    private String qname="";
    private String op="";
    private String pname="";
    private Object value=(String)"";

    public QueryParameter(){
    }

    public String getQname() {
        return qname;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString(){
        String out;
        String format;
        format = "{\"%s %s %s\"}=%s";
        out = String.format(format,qname,op,pname,value);
        return out;
    }
}
