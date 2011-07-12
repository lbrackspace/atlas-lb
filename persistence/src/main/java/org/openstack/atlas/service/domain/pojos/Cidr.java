package org.openstack.atlas.service.domain.pojos;

import java.io.Serializable;
public class Cidr
    implements Serializable
{

    private final static long serialVersionUID = 532512316L;
    protected String block;
    public String getBlock() {
        return block;
    }
    public void setBlock(String value) {
        this.block = value;
    }

    public Cidr() {
    }

    public Cidr(String block) {
        this.block = block;
    }

}
