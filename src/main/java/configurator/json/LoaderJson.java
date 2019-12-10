package configurator.json;

import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.json.JsonValue;

import configurator.ConfiguratorException;
import configurator.ConfiguratorSettings;
import configurator.TypedProperty;
import configurator.annotations.ConfigJson;
import configurator.enums.JsonOperationTypeValue;
import configurator.enums.RuntimeCheckType;
import configurator.utils.ConfigUtils;
import configurator.utils.VerboseLogger;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LoaderJson {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Properties jsonProperties = settings.getJsonProperties();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	public static TypedProperty loadJson(InjectionPoint ip, Class<?> jsonClass) {
		
		ConfigJson configJsonAnn = ip.getAnnotated().getAnnotation(ConfigJson.class);
		
		String propertyName = configJsonAnn.property();
		String jsonFilePath = configJsonAnn.filePath();
		String jsonUrl = configJsonAnn.url();
		String jsonClassMember = configJsonAnn.classMember();
		
		String name = null;
		Object json = null;
		Object defaultValue = null; 
		JsonOperationType type = null;
		
		if(propertyName != null && !propertyName.equals("")) {
			loggerVerbose.log("PROPERTY -> json type passed: " + jsonClass.getSimpleName() + ", property: " + propertyName);
			type = JsonOperationType.createPropertyType(propertyName);
			name = propertyName;
		
		} else if(jsonFilePath != null && !jsonFilePath.equals("")) {
			loggerVerbose.log("FILE_PATH -> json type passed: " + jsonClass.getSimpleName() + ", jsonFilePath: " + jsonFilePath);
			type = JsonOperationType.createFilePathType(jsonFilePath);
			name = jsonFilePath;
			
		} else if(jsonUrl != null && !jsonUrl.equals("")) {
			loggerVerbose.log("URL -> json type passed: " + jsonClass.getSimpleName() + ", jsonURL: " + jsonUrl);
			type = JsonOperationType.createUrlType(jsonUrl);
			name = jsonUrl;
			
		} else if(jsonClassMember != null && !jsonClassMember.equals("")) {
			loggerVerbose.log("CLASS MEMBER -> json type passed: " + jsonClass.getSimpleName() + ", member name: " + jsonClassMember);
			type = JsonOperationType.createClassMemberType(jsonClassMember);
			Class<?> classToInstantiate = ip.getMember().getDeclaringClass();
			type.setAdditionalInfo(classToInstantiate.getName());
			name = jsonClassMember;
			
		} else { 		// if no attribute is set.
			return LoaderJson.noAttributeSetNullJson(ip, jsonClass);
		}
		
		json = LoaderJson.getJsonValue(ip, type, jsonClass);
		defaultValue = LoaderJson.getJsonDefaultValue(ip, jsonClass);
		return new TypedProperty(name, json, defaultValue);
	}
	
	
	
	// Used when no attribute set (property, filePath, url, or classMember) / for testing default values
	private static TypedProperty noAttributeSetNullJson(InjectionPoint ip, Class<?> jsonClass) {
		
		String injectionClassName = ip.getMember().getDeclaringClass().getName();
		String injectionFieldName = ip.getMember().getName();
		
		Object parsedDefaultValue =  LoaderJson.getJsonDefaultValue(ip, jsonClass);
		
		String excMessage = String.format("Any of the @ConfigJson annotation's attributes (property, filePath, url, or classMember) is not set. Class: '%s', field name: '%s'", injectionClassName, injectionFieldName);
		if(settings.isAllowDeploymentWithExceptions() == true) {
			logger.severe("Configurator: " + excMessage);
			return new TypedProperty(null, null, parsedDefaultValue);
		} else {
			throw new ConfiguratorException(excMessage);
		}
		
	}
	
	
	// Loads and then parse to the type or null - props, file, url, memeber
	public static Object getJsonValue(InjectionPoint ip, JsonOperationType type, Class<?> jsonClass) {
		loggerVerbose.log("GET JSON VALUE -> for type: " + type.getValueType().getValue());
		
		ConfigJson configJsonAnn = ip.getAnnotated().getAnnotation(ConfigJson.class);
		
		JsonOperationTypeValue typeValue = type.getValueType();
		String name = type.getAttributeValue();
		RuntimeCheckType runtimeCheckAnn = configJsonAnn.runtimeCheck();
		
		// Try from properties first. The key is the name or for a class members it is className.name
		String propertyKey = !type.getAttributeType().equals("classMember") ? name : (ip.getMember().getDeclaringClass().getName() + "." + name);
		// Use the value from properties if key exists and proper type, otherwise continue loading, even if should use properties (rt check false).
		if(ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), runtimeCheckAnn) == false) {
			
			Object propertyValue = JsonUtils.checkPropertiesIfObjectExistsAndProperJsonType(jsonProperties, propertyKey, jsonClass);
			if(propertyValue != null) {
				loggerVerbose.log("GET JSON VALUE |"+ type.getAttributeType() +"| -> NO LOADING!, Returning json from Properties.");
				return propertyValue;
			}
			
		}
		
		// Loading if runtime true or not found in jsonProperties
		Object loadedJson = null;
		
		try {
			
			if(typeValue == JsonOperationTypeValue.PROPERTY) {
				loadedJson = JsonUtils.findJsonPropertyValue(name, runtimeCheckAnn);
			} else if (typeValue == JsonOperationTypeValue.FILE) {
				loadedJson = JsonUtils.loaderJsonFromFile(name);
			} else if(typeValue == JsonOperationTypeValue.URL) {
				loadedJson = JsonUtils.loaderJsonFromUrl(name);
			} else if(typeValue == JsonOperationTypeValue.CLASS_MEMBER) { 
				String classNameToInstantiate = type.getAdditionalInfo();
				loadedJson = JsonUtils.loaderJsonFromClassMember(name, classNameToInstantiate);
			}
			
		} catch (Exception e) {
			String excMessage = String.format("%s for the %s loader. %s`s attribute: '%s = %s', class: '%s', field name: '%s'.",
					e.getMessage(), type.getValueType().getValue(), type.getAnnotationType(), type.getAttributeType(),
					type.getAttributeValue(), ip.getMember().getDeclaringClass().getName(), ip.getMember().getName());
			if(settings.isAllowDeploymentWithExceptions() == true) {
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}

		}
		loggerVerbose.log("GET JSON VALUE -> after loading, class needed: " + jsonClass.getSimpleName() + ", loaded value: " + loadedJson+ ", class: " + (loadedJson == null? "null": loadedJson.getClass()));
		
		Object parsedJson = null;
		
		if(loadedJson == null) {
			loggerVerbose.log("GET JSON VALUE -> value is null.");
			return null;													// loadedJson is a JSON object, String is needed
		} else if(loadedJson instanceof JsonValue && jsonClass == String.class) {
			loggerVerbose.log("GET JSON VALUE -> value is a JSON object, String needed, returning object.toString().");
			parsedJson = loadedJson.toString();								// if proper class, except String which needs structure check.
		} else if(jsonClass.isInstance(loadedJson) && !(loadedJson instanceof String)) {
			loggerVerbose.log("GET JSON VALUE -> value is a proper type, returning.");
			parsedJson = loadedJson;				
		} else {
			loggerVerbose.log("GET JSON VALUE -> value is not a proper type or String (for value as String needs structure check), parsing.");
			
			Object parsedObj = JsonUtils.parseJsonFromString(ip, loadedJson.toString(), type, jsonClass);
			loggerVerbose.log("GET JSON VALUE -> parsed obj: " + parsedObj + ", class: " + (parsedObj == null? "null": parsedObj.getClass()));
			
			if (parsedObj == null) {
				return null;
			} else if (jsonClass.isInstance(parsedObj)) {
				loggerVerbose.log("GET JSON VALUE -> parsed obj is proper type: " + parsedObj + ", class: " + (parsedObj == null? "null": parsedObj.getClass()));
				parsedJson = parsedObj;				// parsed json is a proper type
			} else if (jsonClass == String.class) {	// after parsing we know the structure is ok
				loggerVerbose.log("GET JSON VALUE -> type is String returning as toString()");
				return parsedObj.toString();
			}
		}
		
		// add to jsonProperties only if not already there and not JsonOperationTypeValue.PROPERTY (props are ENV, Sys so cant be added)
		if(parsedJson != null && !(typeValue == JsonOperationTypeValue.PROPERTY)) {
			// Just info 												// no key already so it is loaded 1st time.
			if (!jsonProperties.containsKey(propertyKey)) {
				loggerVerbose.log("GET JSON VALUE |"+ type.getAttributeType() +"| -> Added to jsonProperties, key: " + propertyKey + ", value: " + loadedJson + ", class: " + loadedJson.getClass());
			} else {													// runtime check true, otherwise it would use props
				loggerVerbose.log("GET JSON VALUE |"+ type.getAttributeType() +"| -> Replaced in jsonProperties, key: " + propertyKey + ", value: " + loadedJson + loadedJson + ", class: " + loadedJson.getClass());
			}
			jsonProperties.put(propertyKey, parsedJson);
			return parsedJson;
		} else if(typeValue == JsonOperationTypeValue.PROPERTY) {		// property not added to jsonProperties
			loggerVerbose.log("GET JSON VALUE |"+ type.getAttributeType() +"| -> value from properties , not added to jsonProperties");
			return parsedJson;
		} else {
			return null;
		}
		
	}
	
	

	public static Object getJsonDefaultValue(InjectionPoint ip, Class<?> jsonClass) {
		
		ConfigJson configJsonAnn = ip.getAnnotated().getAnnotation(ConfigJson.class);
		
		String stringDefaultValue = configJsonAnn.defaultValue();
		String propertyDefaultValue = configJsonAnn.defaultValueProperty();
		String fileDefaultValue = configJsonAnn.defaultValueFile();
		String urlDefaultValue = configJsonAnn.defaultValueUrl();
		String classMemberDefaultValue = configJsonAnn.defaultValueIsClassMember();
		
		JsonOperationType type = null;
		
		if(stringDefaultValue != null && !stringDefaultValue.equals("")) {
			type = JsonOperationType.createDefaultValueType(stringDefaultValue);
		} else if(propertyDefaultValue != null && !propertyDefaultValue.equals("")) {
			type = JsonOperationType.createPropertyDefaultValueType(propertyDefaultValue);
		} else if(fileDefaultValue != null && !fileDefaultValue.equals("")) {
			type = JsonOperationType.createFileDefaultValueType(fileDefaultValue);
		} else if(urlDefaultValue != null && !urlDefaultValue.equals("")) {
			type = JsonOperationType.createUrlDefaultValueType(urlDefaultValue);
		} else if(classMemberDefaultValue != null && !classMemberDefaultValue.equals("")) {
			type = JsonOperationType.createClassMemberDefaultValueType(classMemberDefaultValue);
			Class<?> classToInstantiate = ip.getMember().getDeclaringClass();
			type.setAdditionalInfo(classToInstantiate.getName());
		} else {
			return null;
		}
		
		return LoaderJson.loadDefaultValues(ip, type, jsonClass);
	}
	
	
	
	private static Object loadDefaultValues(InjectionPoint ip, JsonOperationType type, Class<?> jsonClass) {
		loggerVerbose.log("DEFULT VALUE LOADER JSON -> for type: " + type.getValueType().getValue());
		
		ConfigJson configJsonAnn = ip.getAnnotated().getAnnotation(ConfigJson.class);
		RuntimeCheckType runtimeCheckAnn = configJsonAnn.runtimeCheck();
		
		JsonOperationTypeValue typeValue = type.getValueType();
		String name = type.getAttributeValue();
		
		
		// Try from properties first. The key is the name or for a class members it is className.name
		String propertyKey = !type.getAttributeType().equals("defaultValueIsClassMember") ? name : (ip.getMember().getDeclaringClass().getName() + "." + name);
		
		loggerVerbose.log("DEFULT VALUE LOADER JSON -> propertyKey: " + propertyKey);
		
		// Use from properties if the key exists and proper type, otherwise continue loading, even if should use properties (rt check false).
		if(ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), runtimeCheckAnn) == false) {
			
			Object propertyValue = JsonUtils.checkPropertiesIfObjectExistsAndProperJsonType(jsonProperties, propertyKey, jsonClass);
			if(propertyValue != null) {
				loggerVerbose.log("DEFULT VALUE LOADER JSON |"+ type.getAttributeType() +"| -> NO LOADING!, Returning json from Properties.");
				return propertyValue;
			}
			
		}
		
		// Loading if runtime true or not found in jsonProperties
		Object loadedJsonDefaultValue = null;
		
		try {
			
			if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_STRING) {				// null, String, json obj (jsonProperties)
				loadedJsonDefaultValue = JsonUtils.loaderJsonFromString(name);
			} else if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_PROPERTY) {		// null, json obj
				// if property value and runtime check true - dont reload, value will. Otherwise use annotation's runtime check
				if ((configJsonAnn.property() != null && !configJsonAnn.property().equals("")) &&
						ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), runtimeCheckAnn) == true) {
					loggerVerbose.log("DefVal - properties value true and runtime true - no reloading, value does this");
					loadedJsonDefaultValue = JsonUtils.findJsonPropertyValue(name, RuntimeCheckType.NO);
				} else {
					loggerVerbose.log("DefVal - no properties value or runtime false, uses annotation runtime");
					loadedJsonDefaultValue = JsonUtils.findJsonPropertyValue(name, runtimeCheckAnn);
				}
				
			} else if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_FILE) {			// null, String
				loadedJsonDefaultValue = JsonUtils.loaderJsonFromFile(name);
			} else if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_URL) {	// null, String,
				loadedJsonDefaultValue = JsonUtils.loaderJsonFromUrl(name);
			} else if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_CLASS_MEMBER) {	// null, String, 
				String classNameToInstantiate = type.getAdditionalInfo();
				loadedJsonDefaultValue = JsonUtils.loaderJsonFromClassMember(name, classNameToInstantiate);
			}
			
		} catch (Exception e) {
			String excMessage = String.format("%s for the %s loader. %s`s attribute: '%s = %s', class: '%s', field name: '%s'.",
					e.getMessage(), type.getValueType().getValue(), type.getAnnotationType(), type.getAttributeType(),
					type.getAttributeValue(), ip.getMember().getDeclaringClass().getName(), ip.getMember().getName());
			if(settings.isAllowDeploymentWithExceptions() == true) {
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}

		}
		loggerVerbose.log("DEFULT VALUE LOADER JSON -> after loading, class needed: " + jsonClass.getSimpleName() + ", loaded value: " + loadedJsonDefaultValue+ ", class: " + (loadedJsonDefaultValue == null? "null": loadedJsonDefaultValue.getClass()));
		
		Object parsedJson = null;
		
		if(loadedJsonDefaultValue == null) {
			loggerVerbose.log("DEFULT VALUE LOADER JSON -> value is null.");
			return null;													// loadedJson is a JSON obj and String is needed
		} else if(loadedJsonDefaultValue instanceof JsonValue && jsonClass == String.class) {
			loggerVerbose.log("DEFULT VALUE LOADER JSON -> value is a JSON object, String needed, returning object.toString().");
			parsedJson = loadedJsonDefaultValue.toString();					// if proper class, except String which needs structure check.
		} else if(jsonClass.isInstance(loadedJsonDefaultValue) && !(loadedJsonDefaultValue instanceof String)) {
			loggerVerbose.log("DEFULT VALUE LOADER JSON -> value is a proper type, returning.");
			parsedJson = loadedJsonDefaultValue;				
		} else {
			loggerVerbose.log("DEFULT VALUE LOADER JSON -> value is not a proper type or String (for value as String needs structure check), parsing.");
			
			Object parsedObj = JsonUtils.parseJsonFromString(ip, loadedJsonDefaultValue.toString(), type, jsonClass);
			loggerVerbose.log("DEFULT VALUE LOADER JSON -> parsed obj: " + parsedObj + ", class: " + (parsedObj == null? "null": parsedObj.getClass()));
			
			if (parsedObj == null) {
				return null;
			} else if (jsonClass.isInstance(parsedObj)) {
				loggerVerbose.log("DEFULT VALUE LOADER JSON -> parsed obj is proper type: " + parsedObj + ", class: " + (parsedObj == null? "null": parsedObj.getClass()));
				parsedJson = parsedObj;				// parsed json is a proper type
			} else if (jsonClass == String.class) {	// after parsing we know the structure is ok
				loggerVerbose.log("DEFULT VALUE LOADER JSON -> type is String returning as toString()");
				return parsedObj.toString();
			}
		}
		
		// add to jsonProperties only if not there and not JsonOperationTypeValue.DEFAULT_VALUE_PROPERTY (props are ENV, Sys so cant be added)
		if(parsedJson != null && !(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_PROPERTY)) {
			// Just info 												// no key already so it is loaded 1st time.
			if (!jsonProperties.containsKey(propertyKey)) {
				loggerVerbose.log("DEFULT VALUE LOADER JSON |"+ type.getAttributeType() +"| -> Added to jsonProperties, key: " + propertyKey + ", value: " + loadedJsonDefaultValue + ", class: " + loadedJsonDefaultValue.getClass());
			} else {													// runtime check true, otherwise it would use props
				loggerVerbose.log("DEFULT VALUE LOADER JSON |"+ type.getAttributeType() +"| -> Replaced in jsonProperties, key: " + propertyKey + ", value: " + loadedJsonDefaultValue + ", class: " + loadedJsonDefaultValue.getClass());
			}
			jsonProperties.put(propertyKey, parsedJson);
			return parsedJson;
		} else if(typeValue == JsonOperationTypeValue.DEFAULT_VALUE_PROPERTY) {			// property not added to jsonProperties
			loggerVerbose.log("DEFULT VALUE LOADER JSON |"+ type.getAttributeType() +"| -> default value from properties , not added to jsonProperties");
			return parsedJson;
		} else {
			return null;
		}
		
	}
	
}
