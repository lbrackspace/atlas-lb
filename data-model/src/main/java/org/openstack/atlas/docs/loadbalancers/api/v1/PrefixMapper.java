package org.openstack.atlas.docs.loadbalancers.api.v1;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PrefixMapper extends NamespacePrefixMapper {
    private final Log LOG = LogFactory.getLog(PrefixMapper.class);
    private final String atlasNamespaceUri = "http://docs.openstack.org/loadbalancers/api/v1.0";
    private final String atomNamespaceUri = "http://www.w3.org/2005/Atom";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        LOG.debug(String.format("TESTING ====> %s, %s, %b", namespaceUri, suggestion, requirePrefix));
        if (namespaceUri.equals(atlasNamespaceUri)) return "";
        if (namespaceUri.equals(atomNamespaceUri)) return "atom";
        return namespaceUri;
    }
}
