package configurator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import configurator.utils.VerboseLogger;

@ApplicationScoped
public class ConfiguratorSettings {
	
	private static ConfiguratorSettings settings;
	
	private final Logger logger = Logger.getLogger("ConfigLogger");
	
	private boolean verbose = false;
	
	private final Properties loadedProperties = new Properties();
	private final Properties jsonProperties = new Properties();
	
	private final HashSet<String> propertiesPaths = new HashSet<>();
	private final HashSet<String> jsonPropertiesPaths = new HashSet<>();
	private final HashSet<String> jsonFilePaths = new HashSet<>();
	private final HashSet<String> jsonUrls = new HashSet<>();
	private final Map<Class<?>, HashSet<String>> jsonMembers = new HashMap<>();
	
	private boolean runtimeCheck = false;
	private boolean allowDeploymentWithExceptions = false;
	
	private int connectionTimout = 1000;
	private int readTimout = 1000;
	
	
	public static ConfiguratorSettings getInstance() {
		if(settings != null) {
			return settings;
		} else {
			settings = new ConfiguratorSettings();
			return settings;
		}
			
	}
		
	
	public Logger getLogger() {
		return logger;
	}
	
	public VerboseLogger getVerboseLogger() {
		return createVerboseLogger();
	}
	private VerboseLogger createVerboseLogger() {
		
		if(this.verbose == true)
			//return System.out::println;
			//return System.err::println;
			//return Logger.getLogger(this.getClass().getName())::severe;
			return Logger.getLogger(this.getClass().getName())::warning;
			//return Logger.getLogger(this.getClass().getName())::info;
		else
			return s -> {};
	}
	
	public Properties getLoadedProperties() {
		return loadedProperties;
	}
	public Properties getJsonProperties() {
		return jsonProperties;
	}
	
	
	public HashSet<String> getPropertiesPaths() {
		return propertiesPaths;
	}
	public void setPropertiesPaths(HashSet<String> paths) {
		this.propertiesPaths.addAll(paths);
	}
	
	public HashSet<String> getJsonPropertiesPaths() {
		return jsonPropertiesPaths;
	}
	public void setJsonPropertiesPaths(HashSet<String> paths) {
		this.jsonPropertiesPaths.addAll(paths);
	}
	
	public HashSet<String> getJsonFilePaths() {
		return jsonFilePaths;
	}
	public void setJsonFilePaths(HashSet<String> paths) {
		this.jsonFilePaths.addAll(paths);
	}
	
	public HashSet<String> getJsonUrls() {
		return jsonUrls;
	}
	public void setJsonUrls(HashSet<String> paths) {
		this.jsonUrls.addAll(paths);
	}
	
	public Map<Class<?>, HashSet<String>> getJsonMembers() {
		return jsonMembers;
	}
	public void setJsonMembers(Class<?> key, HashSet<String> value) {
		this.jsonMembers.put(key, value);
	}
	
	
	public boolean isRuntimeCheck() {
		return runtimeCheck;
	}
	public void setRuntimeCheck(boolean runtimeCheck) {
		this.runtimeCheck = runtimeCheck;
	}


	public boolean isAllowDeploymentWithExceptions() {
		return allowDeploymentWithExceptions;
	}
	public void setAllowDeploymentWithExceptions(boolean allowDeploymentWithExceptions) {
		this.allowDeploymentWithExceptions = allowDeploymentWithExceptions;
	}
	
	
	public int getConnectionTimout() {
		return connectionTimout;
	}
	public void setConnectionTimout(int connectionTimout) {
		this.connectionTimout = connectionTimout;
	}


	public int getReadTimout() {
		return readTimout;
	}
	public void setReadTimout(int readTimout) {
		this.readTimout = readTimout;
	}


	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	
}
