package org.openstack.atlas.atom.pojo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://www.w3.org/2005/Atom" , name = "entry")
@XmlSeeAlso({LBaasUsagePojo.class,UsageV1Pojo.class})
public class EntryPojo {
    public String title;
    public String author;
    public UsageV1Pojo content;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(UsageV1Pojo content) {
        this.content = content;
    }
}
