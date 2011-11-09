package org.openstack.atlas.api.resource;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.openstack.atlas.api.config.ConfigHelper;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.datamodel.extensions.json.ExtensionWrapper;
import org.openstack.atlas.datamodel.extensions.json.ExtensionsWrapper;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Scope("request")
public class ExtensionsResource {

    @GET
    @Produces({APPLICATION_XML})
    public Response retrieveExtensionsAsXml() {
        try {
            Document root = readFileToXmlDom("extensions.xml");
            root = addXmlExtensions(root);
            String xmlString = documentToString(root);
            return Response.status(Response.Status.OK).entity(xmlString).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Produces({APPLICATION_JSON})
    public Response retrieveExtensionsAsJson() {
        try {
            Gson gson = new Gson();
            ExtensionsWrapper extensionsWrapper = getExtensionsWrapper();
            extensionsWrapper = addJsonExtensions(extensionsWrapper);
            return Response.status(Response.Status.OK).entity(gson.toJson(extensionsWrapper.getExtensions())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseFactory.getErrorResponse(e);
        }
    }

    private Set<String> getExtensionFiles(String format) {
        List<String> enabledExtensions = ConfigHelper.getExtensionPrefixesFromConfiguration();
        if (enabledExtensions.isEmpty()) return new HashSet<String>();

        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        for (String enabledExtension : enabledExtensions) {
            configBuilder.addUrls(ClasspathHelper.forPackage("org.openstack.atlas." + enabledExtension + ".extensions"));
        }

        Reflections reflections = new Reflections(configBuilder.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner()));
        return reflections.getResources(Pattern.compile("extension." + format.toLowerCase()));
    }

    private ExtensionsWrapper addJsonExtensions(ExtensionsWrapper extensionsWrapper) {
        Set<String> jsonFiles = getExtensionFiles("json");

        for (String jsonFile : jsonFiles) {
            ExtensionWrapper extensionWrapper = getExtensionWrapper(jsonFile);
            extensionsWrapper.getExtensions().getExtensions().add(extensionWrapper.getExtension());
        }

        return extensionsWrapper;
    }

    private ExtensionsWrapper getExtensionsWrapper() {
        String file = "extensions.json";
        JsonReader reader = new JsonReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(file)));
        return new Gson().fromJson(reader, ExtensionsWrapper.class);
    }

    private ExtensionWrapper getExtensionWrapper(String file) {
        JsonReader reader = new JsonReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(file)));
        return new Gson().fromJson(reader, ExtensionWrapper.class);
    }

    private Document addXmlExtensions(Document root) {
        List<Document> extensions = new ArrayList<Document>();
        Set<String> xmlFiles = getExtensionFiles("xml");

        for (String xmlFile : xmlFiles) {
            extensions.add(readFileToXmlDom(xmlFile));
        }

        for (Document extension : extensions) {
            try {
                Node extensionNode = root.importNode(extension.getDocumentElement(), true);
                root.getDocumentElement().appendChild(extensionNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return root;
    }

    private String documentToString(Document document) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        return writer.writeToString(document);
    }

    public Document readFileToXmlDom(String file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(file));
            doc.getDocumentElement().normalize();

            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
