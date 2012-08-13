package org.openstack.atlas.restclients.auth.objects.CustomObjects;

import javax.xml.bind.annotation.XmlRootElement;
import org.openstack.atlas.restclients.auth.objects.UserCredentials;

// Jaxb is acting retarded and refuses to generate root elements for these classes
@XmlRootElement(name="credentials",namespace = "http://docs.rackspacecloud.com/auth/api/v1.1")
public class RootElementUserCredentials extends UserCredentials{
}
