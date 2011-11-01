package org.openstack.atlas.rax.api.mapper.dozer.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.v1.extensions.rax.ObjectFactory;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public final class ExtensionObjectMapper {
    private static Log LOG = LogFactory.getLog(ExtensionObjectMapper.class.getName());

    // TODO: Should assert against proper namespace?
    public static <T> T getOtherAttribute(Map<QName, String> otherAttributes, String attributeName) {
        T otherAttribute = null;
        for (QName qname : otherAttributes.keySet()) {
            String value = otherAttributes.get(qname);
            String key = qname.getLocalPart();
            LOG.debug("Attribute: " + key + " : " + value);
            if (key.equalsIgnoreCase(attributeName)) {
                otherAttribute = (T) value;
            }
        }
        return otherAttribute;
    }

    public static <T> T getAnyElement(List<Object> anies, Class<T> classType) {
        for (Object any : anies) {
            LOG.debug("Class: " + any.getClass());

            if (any instanceof Element) {
                Element element = (Element) any;
                try {
                    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    Object o = unmarshaller.unmarshal(element);
                    if(classType.isInstance(o)) {
                        return classType.cast(o);
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
}
