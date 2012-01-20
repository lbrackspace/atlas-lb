package org.openstack.atlas.util.ca.zeus;

import java.util.ArrayList;
import java.util.List;

public class ZeusCertFile {

    private String private_key;
    private String public_cert;
    private List<String> errorList;
    public ZeusCertFile() {
        errorList = new ArrayList<String>();
    }
    public boolean isError(){
        return (errorList == null || errorList.size()>0);
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getPublic_cert() {
        return public_cert;
    }

    public void setPublic_cert(String public_cert) {
        this.public_cert = public_cert;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

}
