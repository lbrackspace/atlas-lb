package org.openstack.atlas.api.extension;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class XmlExtensionDefinitionReader {

    public static Document readAsXmlDocument(String file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XmlExtensionDefinitionReader.class.getClassLoader().getResourceAsStream(file));
            doc.getDocumentElement().normalize();

            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String documentToString(Document document) throws Exception {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            return writer.writeToString(document);
        } catch (Exception e) {
            throw new Exception("Error converting the xml document to String.", e);
        }
    }

    public static Set<String> getExtensionFiles(String enabledExtension, String format) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.addUrls(ClasspathHelper.forPackage("org.openstack.atlas." + enabledExtension + ".extensions"));

        Reflections reflections = new Reflections(configBuilder.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner()));
        return reflections.getResources(Pattern.compile("extension." + format.toLowerCase()));
    }

    public static String getExtensionFile(String prefix, String extension, String format) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.addUrls(ClasspathHelper.forPackage("org.openstack.atlas." + prefix + ".extensions." + extension));

        Reflections reflections = new Reflections(configBuilder.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner()));
        Set<java.lang.String> files = reflections.getResources(Pattern.compile("extension." + format.toLowerCase()));
        return files.iterator().next();
    }

    public static String readFileAsString(String fileName) throws Exception {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XmlExtensionDefinitionReader.class.getClassLoader().getResourceAsStream(fileName));
            doc.getDocumentElement().normalize();

            return documentToString(doc);
        } catch (Exception e) {
            throw new Exception("Error reading the extension file.", e);
        }
    }
}
