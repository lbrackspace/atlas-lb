package org.openstack.atlas.api.config;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginContextLoaderListener extends ContextLoaderListener {
    @Override
    protected void customizeContext(ServletContext servletContext,
                                    ConfigurableWebApplicationContext wac) {
        System.out.println("Init Plugin");
        String locationPrefix = "classpath:";
        String locationSuffix = "-spring-context-public.xml";

        List<String> configLocations = new ArrayList<String>();
        configLocations.addAll(Arrays.asList(wac.getConfigLocations()));
        configLocations.add("classpath:spring-context-public.xml");

        List<String> pluginsTurnedOn = ConfigHelper.getPluginsFromConfiguration();
        for (String pluginName : pluginsTurnedOn) {
            String location = locationPrefix + pluginName + locationSuffix;
            //if(new File(location).exists()) {
            configLocations.add(location);
            //}
        }

        wac.setConfigLocations(configLocations.toArray(new String[configLocations.size()]));
    }

}
