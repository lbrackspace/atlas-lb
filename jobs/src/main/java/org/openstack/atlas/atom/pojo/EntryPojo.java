package org.openstack.atlas.atom.pojo;

import org.openstack.atlas.jobs.UsageEntry;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(namespace = "http://www.w3.org/2005/Atom", name = "entry")
@XmlSeeAlso({LBaaSUsagePojo.class})

public class EntryPojo extends UsageEntry {
}
