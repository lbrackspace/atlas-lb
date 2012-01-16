package org.openstack.atlas.api.extension;

import com.google.gson.Gson;
import org.openstack.atlas.api.config.PluginConfiguration;
import org.openstack.atlas.datamodel.extensions.json.ExtensionWrapper;
import org.openstack.atlas.datamodel.extensions.json.ExtensionsWrapper;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

@Service
public class ExtensionService {
    public String getExtensionsAsXml() throws Exception {
        String enabledExtension = PluginConfiguration.getExtensionPrefix();
        Document extensions = XmlExtensionDefinitionReader.readAsXmlDocument("extensions.xml");

        Set<String> xmlFiles = XmlExtensionDefinitionReader.getExtensionFiles(enabledExtension, "xml");
        for (String file : xmlFiles) {
            Document extension = XmlExtensionDefinitionReader.readAsXmlDocument(file);
            Node extensionNode = extensions.importNode(extension.getDocumentElement(), true);
            extensions.getDocumentElement().appendChild(extensionNode);
        }

        return XmlExtensionDefinitionReader.documentToString(extensions);
    }

    public String getExtensionAsXml(String alias) throws Exception {
        String enabledExtension = PluginConfiguration.getExtensionPrefix();
        String[] arr = alias.toLowerCase().split("-atlas-"); // eg. alias = "RAX-ATLAS-AL";
        String prefix = arr[0];
        String extensionName = arr[1];
        if (!enabledExtension.equalsIgnoreCase(prefix)) {
            throw new BadRequestException("Extension " + alias + " not found");
        }

        String fileName = XmlExtensionDefinitionReader.getExtensionFile(prefix, extensionName, "xml");
        return XmlExtensionDefinitionReader.readFileAsString(fileName);
    }

    public String getExtensionsAsJson() throws Exception {
        String enabledExtension = PluginConfiguration.getExtensionPrefix();
        ExtensionsWrapper extensionsWrapper = JsonExtensionDefinitionReader.getExtensionsWrapper("extensions.json");
        Set<String> jsonFiles = JsonExtensionDefinitionReader.getExtensionFiles(enabledExtension, "json");

        for (String file : jsonFiles) {
            ExtensionWrapper extension = JsonExtensionDefinitionReader.getExtensionWrapper(file);
            extensionsWrapper.getExtensions().getExtensions().add(extension.getExtension());
        }

        Gson gson = new Gson();
        return gson.toJson(extensionsWrapper.getExtensions());

    }

    public String getExtensionAsJson(String alias) throws Exception {
        String enabledExtension = PluginConfiguration.getExtensionPrefix();
        String[] arr = alias.toLowerCase().split("-atlas-"); // eg. alias = "RAX-ATLAS-AL";
        String prefix = arr[0];
        String extensionName = arr[1];
        if (!enabledExtension.equalsIgnoreCase(prefix)) {
            throw new BadRequestException("Extension " + alias + " not found");
        }
        String fileName = JsonExtensionDefinitionReader.getExtensionFile(prefix, extensionName, "json");
        ExtensionWrapper extensionWrapper = JsonExtensionDefinitionReader.getExtensionWrapper(fileName);
        Gson gson = new Gson();
        return gson.toJson(extensionWrapper.getExtension());
    }
}
