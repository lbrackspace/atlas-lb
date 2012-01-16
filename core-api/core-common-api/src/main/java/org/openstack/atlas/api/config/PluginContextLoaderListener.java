package org.openstack.atlas.api.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginContextLoaderListener extends ContextLoaderListener {

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext wac) {
        List<String> contexts = new ArrayList<String>();
        contexts.addAll(Arrays.asList(wac.getConfigLocations()));

        //List<String>  commonContexts = PluginConfiguration.getCommonContexts();
        //commonContexts = PluginConfiguration.classpathify(commonContexts);
        //contexts.addAll(commonContexts);

        String extensionName = PluginConfiguration.getExtensionPrefix();
        String adapterName = PluginConfiguration.getAdapterPrefix();
        if (StringUtils.isEmpty(extensionName)) {
            List<String>  coreContexts = PluginConfiguration.getCoreContexts(adapterName);
            coreContexts.add("dozer-context.xml");
            coreContexts = PluginConfiguration.classpathify(coreContexts);
            contexts.addAll(coreContexts);
        } else {
            List<String>  coreContexts = PluginConfiguration.getCoreContexts(adapterName);
            coreContexts = PluginConfiguration.classpathify(coreContexts);
            contexts.addAll(coreContexts);

            List<String>  extensionContexts = PluginConfiguration.getExtensionContexts(extensionName, adapterName);
            extensionContexts.add(extensionName + "-dozer-context.xml");
            extensionContexts = PluginConfiguration.classpathify(extensionContexts);
            contexts.addAll(extensionContexts);
        }
        wac.setConfigLocations(contexts.toArray(new String[contexts.size()]));
    }
}
