package org.openstack.atlas.api.validation;

import java.util.ArrayList;
import java.util.List;

public class ComplexBean {

    private String complexString;
    private List<SimpleBean> mySimpleBeans;

    public ComplexBean() {
    }

    public String getComplexString() {
        return complexString;
    }

    public void setComplexString(String complexString) {
        this.complexString = complexString;
    }

    public void setMySimpleBeans(List<SimpleBean> mySimpleBeans) {
        this.mySimpleBeans = mySimpleBeans;
    }

    public List<SimpleBean> getMySimpleBeans() {

        return mySimpleBeans;
    }

    public void addSimpleBean(SimpleBean simpleBean) {
        if(mySimpleBeans == null) mySimpleBeans = new ArrayList<SimpleBean>();
        mySimpleBeans.add(simpleBean);
    }
}
