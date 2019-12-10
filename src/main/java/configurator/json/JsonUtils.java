package configurator.json;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import configurator.ConfiguratorException;
import configurator.ConfiguratorSettings;
import configurator.enums.RuntimeCheckType;
import configurator.typed.LoaderProperties;
import configurator.utils.ConfigUtils;
import configurator.utils.VerboseLogger;

public class JsonUtils {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Properties loadedProperties = settings.getLoadedProperties();
	private static Properties jsonProperties = settings.getJsonProperties();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	// Looks for values: env / system / jsonPropertie / loadedProperties
	public static Object findJsonPropertyValue(String propToFind, RuntimeCheckType annotationRuntimeCheck) {
		loggerVerbose.log("PROPERTY FINDER JSON -> find method, key passed: " + propToFind);
		
		String envProperty = System.getenv(propToFind);
		if(envProperty != null) {
			loggerVerbose.log("PROPERTY FINDER JSON -> returning from ENV: " + envProperty + ", class: " + envProperty.getClass());
			return envProperty;
		}
		
		
		String sysProperty = System.getProperty(propToFind);
		if(sysProperty != null) {
			loggerVerbose.log("PROPERTY FINDER JSON -> returning from System: " + sysProperty + ", class: " + sysProperty.getClass());
			return sysProperty;
		}
		
		
		// if runtime check true reloads property files.
		if(ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), annotationRuntimeCheck) == true) {
			loggerVerbose.log("PROPERTY FINDER JSON ->  --- RELOADING PROPERTIES ---");
			LoaderProperties.loadPropertiesFromFile(settings.getPropertiesPaths());
			LoaderJsonSetup.loadJsonPropertiesFromPaths(settings.getJsonPropertiesPaths());
			// For file, url and member it is not needed to reload. @ConfigJson reloads needed things.
		}
		
		
		Object jsonProperty = jsonProperties.get(propToFind);
		if(jsonProperty != null) {
			loggerVerbose.log("PROPERTY FINDER JSON -> returning from jsonProperties: " + ConfigUtils.displayStringOfLength(jsonProperty.toString(), 30) + ", class: " + jsonProperty.getClass());
			return jsonProperty;
		}
		
		
		// Always parsed, jsonPropertyFilePath parse, not added to jsonProperties to avoid name clashes
		Object loadedProperty = loadedProperties.get(propToFind);
		if(loadedProperty != null) {
			loggerVerbose.log("PROPERTY FINDER JSON -> returning from loadedProperties: " + ConfigUtils.displayStringOfLength(loadedProperty.toString(), 30) + ", class: " + loadedProperty.getClass());
			return loadedProperty;
		}
		
		
		loggerVerbose.log("PROPERTY FINDER JSON -> property name not found, returning null.");
		return null;
		
	}
	

	
	public static Object parseJsonFromString(InjectionPoint ip, String jsonString, JsonOperationType type, Class<?> jsonClass) {
		loggerVerbose.log("JSON STRING PARSER -> json String passed: " + ConfigUtils.displayStringOfLength(jsonString, 80) + ", for: " + type.getAttributeType());
		
		String injectionAnnotationType = type.getAnnotationType();
		String injectionClassName = ip.getMember().getDeclaringClass().getName();
		String injectionFieldName = ip.getMember().getName();
		
		JsonValue parsedValue = null;
		
		try(JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			
			if(jsonClass == JsonObject.class) {
				parsedValue = reader.readObject();
			} else if (jsonClass == JsonArray.class) {
				parsedValue = reader.readArray();
			} else if (jsonClass == String.class) {		// Read to check if the structure is OK.
				return reader.read().toString();		// If null, exception, no need to check 
			} else {
				String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
				String excMessage = String.format("Method '%s' can NOT parse String for the type: '%s'. %s`s attribute: '%s = %s', class: '%s', field name: '%s'", methodName, jsonClass.getName(), injectionAnnotationType, type.getAttributeType(), type.getAttributeValue(), injectionClassName, injectionFieldName);
				if(settings.isAllowDeploymentWithExceptions() == true) {	// This method is for parsing JsonObject and JsonArray only
					logger.severe("Configurator: " + excMessage);
					return null;
				} else {
					throw new ConfiguratorException(excMessage);
				}
			}
			
		} catch (Exception e) {
			String excMessage = String.format("Error parsing %s for a %s, exception: %s. %s`s attribute: '%s = %s', class: '%s', field name: '%s'", jsonClass.getSimpleName(), type.getValueType().getValue(), e.getMessage(), injectionAnnotationType, type.getAttributeType(), type.getAttributeValue(), injectionClassName, injectionFieldName);
			if(settings.isAllowDeploymentWithExceptions() == true) {		// Value cant be parsed, null or bad structure.
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}
		}
		
		return parsedValue;
	}
	
	
	
	public static Object loaderJsonFromString(String jsonString) {
		loggerVerbose.log("JSON STRING LOADER -> json String passed: " + ConfigUtils.displayStringOfLength(jsonString, 80));
		
		try(JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			return reader.read();
		} catch (Exception e) {
			throw new ConfiguratorException(String.format("Error parsing JSON, exception: '%s',", e.getMessage()));
		}
		
	}
	
	
	public static Object loaderJsonFromFile(String jsonFilePath) {
		loggerVerbose.log("JSON FILE LOADER -> json path passed: " + jsonFilePath);
		
		try(JsonReader reader = Json.createReader(new FileInputStream(jsonFilePath))) {
			
			return reader.read(); 
			
		} catch (Exception e) {
			throw new ConfiguratorException(String.format("Error loading JSON, exception: '%s',", e.getMessage()));
		}
	}
	
	
	public static String loaderJsonFromUrl(String jsonURL) {
		loggerVerbose.log("JSON URL LOADER -> url passed: " + jsonURL);
		
		int responseCode = 0;
		StringBuilder downloadedJson = new StringBuilder();
		
		try {
			
			URL url = new URL(jsonURL);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        
	        responseCode = conn.getResponseCode();
			
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String inputLine;

	        while ((inputLine = in.readLine()) != null) {
	        	downloadedJson.append(inputLine);
	        }
			in.close();
			
		} catch (Exception e) {
			throw new ConfiguratorException(String.format("Error downloading JSON, response status code: '%s', exception: '%s',", responseCode, e.getMessage() ));
		} 
		
		return downloadedJson.toString();
	}
	
	
	public static Object loaderJsonFromClassMember(String memberName, String classNameToInstantiate) {
		loggerVerbose.log("JSON CLASS MEMBER LOADER -> member name: " + memberName + ", class to instantiate: " + classNameToInstantiate);
		
		Class<?> clazz = null;
		
		Object newInstance;
		try {
			clazz = Class.forName(classNameToInstantiate);
			newInstance = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

			throw new ConfiguratorException(String.format("Can not instantiate class: '%s'", clazz.getName()));
			
		}
		
		Method[] declaredMethods = newInstance.getClass().getDeclaredMethods();
		Field[] declaredFields = newInstance.getClass().getDeclaredFields();
		
		Method method = null;
		for(Method m : declaredMethods) {
			if (m.getName().equals(memberName)) {
				method = m;
				break;
			}
		}
		
		Field field = null;
		for(Field f : declaredFields) {
			if (f.getName().equals(memberName)) {
				field = f;
				break;
			}
		}
		
		if(field == null && method == null) {
			throw new ConfiguratorException(String.format("Can not find the field or method named: '%s' in the class: '%s'", memberName, clazz.getName()));
		}
		
		Object loadedMemberObject = null;
		if(method != null) {
			
			boolean accessible = method.isAccessible();
			method.setAccessible(true);
			
			try {
				loadedMemberObject = method.invoke(newInstance);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new ConfiguratorException(String.format("Can not invoke the method: '%s' in the class: '%s'", memberName, clazz.getName()));
			} finally {
				method.setAccessible(accessible);
			}
			
		} else {
			
			boolean accessible = field.isAccessible();
			field.setAccessible(true);
			
			try {
				loadedMemberObject = field.get(newInstance);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ConfiguratorException(String.format("Can not get the value of the field: '%s' in the class: '%s'", memberName, clazz.getName()));
			} finally {
				field.setAccessible(accessible);
			}
			
		}
		
		return loadedMemberObject;
		
	}
	
	
	// When no runtime check it checks if props contains a key and it is a proper type,
	// If the value has a proper class returns it, if not and a String tries to parse it. Otherwise null.
	public static Object checkPropertiesIfObjectExistsAndProperJsonType(Properties properties, String key, Class<?> clazz) {
		loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> props contains key?: " + properties.containsKey(key));
		
		if(properties.containsKey(key)) {
			
			Object value = properties.get(key);
			
			// The value is String class
			if (value != null && clazz == String.class) {
				loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> String class needed, returning object as a String: " + value + ", class: " + value.getClass());
				return value.toString();
			}
			
			// The value is the needed class.
			if (clazz.isInstance(value)) {
				loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> proper class found, returning object: " + value + ", class: " + value.getClass());
				return value;
			}
			
			System.err.println("CHECK JSON IN PROPS AND TYPE -> value is not a proper class");
			
			// If not a proper class and it is a String we try to parse it.
			if (value.getClass() != clazz && value.getClass() == String.class) {
				// try to parse and check the type again, if ok return, if not proceed
				loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> value is not a proper class but it is a String: " + value + ", class: " + value.getClass());
				try (JsonReader reader = Json.createReader(new StringReader((String)value)) ) {
					
					JsonStructure json = reader.read();
					loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> parsed string: " + json + ", class: " + json.getClass());
					
					// If we get an object of a class we need replace it in properties to have a proper one for next time.  
					if (json != null && clazz.isInstance(json)) {
						boolean replaced = properties.replace(key, value, json);
						loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> parsed json has a proper class, replacing it in properties, Operation successful? : " + replaced);
						return json;
					}

				} catch (Exception e) {
					loggerVerbose.log("CHECK JSON IN PROPS AND TYPE -> exception parsing String: " + e.getMessage());
					return null;
				}
			}
		}
		
		return null;
	}
	
	
}
