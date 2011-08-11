package org.openstack.atlas.api.atom;

import org.apache.abdera.factory.ExtensionFactory;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Element;

public class ElementFactory implements ExtensionFactory {
    private String[] namespaces = new String[]{"some_namespsace"};

    @Override
    public boolean handlesNamespace(String s) {
        for (String namespace : namespaces) {
            if (s.equals(namespace)) return true;
        }
        return false;
    }

    @Override
    public String[] getNamespaces() {
        return namespaces;
    }

    @Override
    public <T extends Element> T getElementWrapper(Element element) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends Base> String getMimeType(T t) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
