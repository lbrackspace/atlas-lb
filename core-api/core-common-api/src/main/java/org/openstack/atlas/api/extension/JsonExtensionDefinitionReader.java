package org.openstack.atlas.api.extension;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.openstack.atlas.datamodel.extensions.json.ExtensionWrapper;
import org.openstack.atlas.datamodel.extensions.json.ExtensionsWrapper;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JsonExtensionDefinitionReader {
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

    public static ExtensionsWrapper getExtensionsWrapper(String file) {
        JsonReader reader = new JsonReader(new InputStreamReader(JsonExtensionDefinitionReader.class.getClassLoader().getResourceAsStream(file)));
        return new Gson().fromJson(reader, ExtensionsWrapper.class);
    }

    public static ExtensionWrapper getExtensionWrapper(String file) {
        JsonReader reader = new JsonReader(new InputStreamReader(JsonExtensionDefinitionReader.class.getClassLoader().getResourceAsStream(file)));
        return new Gson().fromJson(reader, ExtensionWrapper.class);
    }
}