package configurator.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;

import configurator.ConfiguratorException;
import configurator.ConfiguratorSettings;
import configurator.utils.ConfigUtils;
import configurator.utils.VerboseLogger;


public class LoaderJsonSetup {
	
	private static final String GENERIC_ERROR_MSG = "Error parsing JSON for the %s loader. @ConfiguratorSetup`s attribute: '%s = %s', exception: %s";
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Properties jsonProperties = settings.getJsonProperties();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	public static void loadJsonPropertiesFromPaths(HashSet<String> jsonPaths) {
		loggerVerbose.log("LOADER_PROPERTIES_JSON -> json props size: " + jsonPaths.size() + ", paths: " + jsonPaths);
		
		for(String path : jsonPaths) {
			
			if (path == null || path.equals(""))	// default is "";
				continue;
			
			Properties props = new Properties();
			
			try (InputStream is = new FileInputStream(path)) {
				props.load(is);
			} catch (IOException e) {
				String excMessage = String.format("Error loading JSON for the properties file loader. @ConfiguratorSetup`s attribute: 'jsonPropertyFilePaths = %s', exception: %s", path, e.getMessage());
				if(settings.isAllowDeploymentWithExceptions() == true) {		// Propery cant be parsed, null or bad structure.
					logger.severe("Configurator: " + excMessage);
				} else {
					throw new ConfiguratorException(excMessage);
				}
			}
			
			for(Object o : props.keySet()) {
				
				JsonOperationType type = JsonOperationType.createJsonPropertiesLoaderType(path, (String)o);
				Object parseJsonFromString = LoaderJsonSetup.parseJsonFromString((String)props.get(o), type);
				
				if(parseJsonFromString != null) {
					loggerVerbose.log("LOADER_PROPERTIES_JSON -> parse from properties successful, parsed: " + ConfigUtils.displayStringOfLength(parseJsonFromString.toString(), 80) + ", class: " + parseJsonFromString.getClass());
					jsonProperties.put(o, parseJsonFromString);
				}
			}
		}
	}
	
	
	public static void loadJsonFromFiles(HashSet<String> jsonFiles) {
		loggerVerbose.log("LOADER_FILES_JSON -> json files size: " + jsonFiles.size() + ", files: " + jsonFiles);
		
		for(String path : jsonFiles) {
			
			if (path == null || path.equals(""))	// default is "";
				continue;
			
			JsonOperationType type = JsonOperationType.createJsonFilesLoaderType(path);
				
			Object parseJsonFromFile = LoaderJsonSetup.parseJsonFromFile(path, type);
				
			if(parseJsonFromFile != null) {
				loggerVerbose.log("LOADER_FILES_JSON -> parse from files successful, parsed: " + ConfigUtils.displayStringOfLength(parseJsonFromFile.toString(), 80) + ", class: " + parseJsonFromFile.getClass());
				jsonProperties.put(path, parseJsonFromFile);
			} 
		}
		
	}
		
	
	public static void loadJsonFromUrls(HashSet<String> jsonUrls) {
		loggerVerbose.log("LOADER_URLS_JSON -> json URLs size: " + jsonUrls.size() + ", urls: " + jsonUrls);
		
		for(String url : jsonUrls) {
			
			if (url == null || url.equals(""))
				continue;
			
			JsonOperationType type = JsonOperationType.createJsonUrlsLoaderType(url);
				
			String loadedJsonFromUrl = null;
			try {
				loadedJsonFromUrl = JsonUtils.loaderJsonFromUrl(url);
				loggerVerbose.log("LOADER_URLS_JSON -> JSON String downloaded successfully, url: '" + url +"', value: " + ConfigUtils.displayStringOfLength(loadedJsonFromUrl, 80));
			} catch (Exception e) {
				String excMessage = String.format("%s for the %s loader. @ConfiguratorSetup`s attribute: '%s = %s'", e.getMessage(), type.getValueType().getValue(), type.getAttributeType(), type.getAttributeValue());
				if(settings.isAllowDeploymentWithExceptions() == true) {
					logger.severe("Configurator: " + excMessage);
					continue;
				} else {
					throw new ConfiguratorException(excMessage);
				}
			}
			
			if(loadedJsonFromUrl != null) {
				Object parseJsonFromString = LoaderJsonSetup.parseJsonFromString(loadedJsonFromUrl, type);
				if(parseJsonFromString != null) {
					loggerVerbose.log("LOADER_URLS_JSON -> JSON String parsed successfully: " + ConfigUtils.displayStringOfLength(parseJsonFromString.toString(), 80) + ", class: " + parseJsonFromString.getClass());
					jsonProperties.put(url, parseJsonFromString);
				}
			} 
		}
	}
	
	
	public static void loadJsonFromMembers(HashSet<String> jsonMembers, Class<?> clazz) {
		loggerVerbose.log("LOADER_CLASS_MEMBERS_JSON -> json class members size: " + jsonMembers.size() + ", members: " + jsonMembers);
		
		for(String member : jsonMembers) {
			
			if (member == null || member.equals(""))	// default is "";
				continue;
				
			String propertyName = clazz.getName() + "." + member; 
			JsonOperationType type = JsonOperationType.createJsonMembersLoaderType(member);
				
			Object loadedJsonFromMember = null;
			try {
				loadedJsonFromMember = JsonUtils.loaderJsonFromClassMember(member, clazz.getName());
				
			} catch (Exception e) {
				String excMessage = String.format("%s for the %s loader. @ConfiguratorSetup`s attribute: '%s = %s'", e.getMessage(), type.getValueType().getValue(), type.getAttributeType(), type.getAttributeValue());
				if(settings.isAllowDeploymentWithExceptions() == true) {
					logger.severe("Configurator: " + excMessage);
					continue;
				} else {
					throw new ConfiguratorException(excMessage);
				}
			}
			
			
			if(loadedJsonFromMember == null) {
				loggerVerbose.log("LOADER_CLASS_MEMBERS_JSON ->  member '"+ member +"' is null");
				continue;
			} else if (!(loadedJsonFromMember instanceof JsonValue)) {							// Not a proper type, trying to parse a String
				loggerVerbose.log("LOADER_CLASS_MEMBERS_JSON -> member '"+ member +"' is not a proper type, trying to parse as a String");
				loadedJsonFromMember = LoaderJsonSetup.parseJsonFromString(loadedJsonFromMember.toString(), type);
				if(loadedJsonFromMember != null) {
					loggerVerbose.log("LOADER_CLASS_MEMBERS_JSON -> member '"+ member +"' parsed successfully, class: " + loadedJsonFromMember.getClass() + ", parsed: " + ConfigUtils.displayStringOfLength(loadedJsonFromMember.toString(), 80));
					jsonProperties.put(propertyName, loadedJsonFromMember);
				}
			} else {
				loggerVerbose.log("LOADER_CLASS_MEMBERS_JSON -> member '"+ member +"' loaded successfully, class: " + loadedJsonFromMember.getClass() + ", loaded: " + ConfigUtils.displayStringOfLength(loadedJsonFromMember.toString(), 80));
				jsonProperties.put(propertyName, loadedJsonFromMember);
			}
			
		}
		
	}
	
	

	private static Object parseJsonFromString(String json, JsonOperationType type) {
		
		try(JsonReader reader = Json.createReader(new StringReader(json))) {
			
			return reader.read();
			
		} catch (Exception e) {
			// additional - for properties only, needs name of the property.
			String additional = type.getAdditionalInfo() == null ? "" : ", property name: '" + type.getAdditionalInfo() + "'";
			String excMessage = String.format("Error parsing JSON String for the %s loader. @ConfiguratorSetup`s attribute: '%s = %s'%s, exception: %s", type.getValueType().getValue(), type.getAttributeType(), type.getAttributeValue(), additional, e.getMessage());
			if(settings.isAllowDeploymentWithExceptions() == true) {		// Propery cant be parsed, null or bad structure.
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}
		}
	}
	
	private static Object parseJsonFromFile(String file, JsonOperationType type) {
		
		try(JsonReader reader = Json.createReader(new FileInputStream(file))) {
			
			return reader.read();
			
		} catch (Exception e) {
			String excMessage = String.format(GENERIC_ERROR_MSG, type.getValueType().getValue(), type.getAttributeType(), type.getAttributeValue(), e.getMessage());
			if(settings.isAllowDeploymentWithExceptions() == true) {		// Propery cant be parsed, null or bad structure.
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}
		}
	}
	
}
