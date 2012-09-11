package org.openstack.atlas.atom.pojo;

import org.w3._2005.atom.UsageEntry;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://www.w3.org/2005/Atom", name = "entry")
@XmlSeeAlso({LBaaSUsagePojo.class})

public class EntryPojo extends UsageEntry {
}
