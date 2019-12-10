package configurator.typed;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;

import configurator.ConfiguratorException;
import configurator.ConfiguratorSettings;
import configurator.enums.RuntimeCheckType;
import configurator.utils.ConfigUtils;
import configurator.utils.VerboseLogger;

public class LoaderProperties {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Properties loadedProperties = settings.getLoadedProperties();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	// Looks for values: env / system / loadedProperties
	public static String findPropertyValue(String propToFind, RuntimeCheckType annotationRuntimeCheck) {
		loggerVerbose.log("PROPERTY FINDER -> find method, key passed: " + propToFind);
		
		String envProperty = System.getenv(propToFind);
		if(envProperty != null) {
			loggerVerbose.log("PROPERTY FINDER -> ENV var found. Key: " + propToFind + ", value: " + envProperty);
			return envProperty;
		}
		
		String sysProperty = System.getProperty(propToFind);
		if(sysProperty != null) {
			loggerVerbose.log("PROPERTY FINDER -> System property found. Key: " + propToFind + ", value: " + sysProperty);
			return sysProperty;
		}
		// Here because no need to reload before dynamic ones
		if(ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), annotationRuntimeCheck) == true) {
			loggerVerbose.log("PROPERTY FINDER -> --- RELOADING PROPERTIES ---");
			LoaderProperties.loadPropertiesFromFile(settings.getPropertiesPaths());
		}
		
		String fileProperty = loadedProperties.getProperty(propToFind);
		if(fileProperty != null) {
			loggerVerbose.log("PROPERTY FINDER -> file property found. Key: " + propToFind + ", value: " + fileProperty);
			return fileProperty;
		}
		
		loggerVerbose.log("PROPERTY FINDER -> property NOT found, returning NULL");
		return null;
	}
	
	
	public static void loadPropertiesFromFile(HashSet<String> paths) {
		loggerVerbose.log("LOADER_FILE_PROPERTIES -> props size: " + paths.size() + ", paths: " + paths);
		
		for(String path : paths) {
			
			if (path == null || path.equals(""))
				continue;
			
			Properties props = new Properties();
			
			try (InputStream is = new FileInputStream(path)) {
				props.load(is);
				loadedProperties.putAll(props);
				
			} catch (IOException e) {
				String excMessage = String.format("Properties file loader can not load file: '%s', exception: %s", path, e.getMessage());
				if(settings.isAllowDeploymentWithExceptions() == true) {
					logger.severe("Configurator: " + excMessage);
				} else {
					throw new ConfiguratorException(excMessage);
				}
			}
		}
		
		loggerVerbose.log("Properties -> size: " + loadedProperties.size() + ", keys: " + loadedProperties.keySet());
	}
	
}
