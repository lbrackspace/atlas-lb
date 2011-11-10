package org.openstack.atlas.api.auth.integration.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *  Cloud Files utilities 
 */
public class FileUtil {
    private static Log LOG = LogFactory.getLog(FileUtil.class);
	/**
	 *  The name of the properties file we're looking for 
	 */
	private final static String file = "test.properties";
	
	/**
	 *  A cache of the properties
	 */
	private static Properties props = null;
	
	/**
	 * Find the properties file in the class path and load it.
	 * 
	 * @throws java.io.IOException
	 */
	private static synchronized void loadPropertiesFromClasspath() throws IOException {
        props = new Properties();
        InputStream inputStream = FileUtil.class.getClassLoader()
            .getResourceAsStream(file);

        if (inputStream == null) {
            throw new FileNotFoundException("Property file '" + file
                + "' not found in the classpath");
        }
        props.load(inputStream);
	}
	
	/**
	 * Look up a property from the properties file.
	 * 
	 * @param key The name of the property to be found
	 * @return    The value of the property
	 */
	public static String getProperty(String key)
	{
		if (props == null)
		{
			try
			{
				loadPropertiesFromClasspath();
			}
			catch (Exception IOException)
			{
				LOG.warn("Unable to load properties file.");
				return null;
			}
		}
		return props.getProperty(key);
	}
	
	/**
	 * Look up a property from the properties file.
	 * 
	 * @param key The name of the property to be found
	 * @return    The value of the property
	 */
        public static String getProperty(String key, String defaultValue)
	{
		if (props == null)
		{
			try
			{
				loadPropertiesFromClasspath();
			}
			catch (Exception IOException)
			{
				LOG.warn("Unable to load properties file.");
				return null;
			}
		}
		return props.getProperty(key, defaultValue);
	}
	
	/**
	 * Looks up the value of a key from the properties file and converts it to an integer.
	 * 
	 * @param key
	 * @return The value of that key
	 */
	public static int getIntProperty(String key) {
		String property = getProperty(key);
		
		if (property == null) {
			LOG.warn("Could not load integer property " + key);
			return -1;
		}
		try {
			return Integer.parseInt(property);
		}
		catch (NumberFormatException nfe) { 
			LOG.warn("Invalid format for a number in properties file: " + property, nfe);
			return -1;
		}
	}
}