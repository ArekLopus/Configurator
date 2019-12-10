package configurator.typed;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.InjectionPoint;

import configurator.ConfiguratorException;
import configurator.ConfiguratorSettings;
import configurator.TypedProperty;
import configurator.annotations.ConfigTyped;
import configurator.enums.RuntimeCheckType;
import configurator.enums.TypedOperationTypeValue;
import configurator.utils.ConfigUtils;
import configurator.utils.VerboseLogger;

@SuppressWarnings({"rawtypes","unchecked"})
public class LoaderTyped {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	public static TypedProperty loadTyped(InjectionPoint ip, Class<?> typedClass) {
		
		ConfigTyped configTypedAnn = ip.getAnnotated().getAnnotation(ConfigTyped.class);
		String propertyName = configTypedAnn.value();
		
		Object loadedValue = LoaderTyped.getTypedValue(ip, typedClass);
		Object loadedDefaultValue = LoaderTyped.getTypedDefaultValue(ip, typedClass);
		
		if(propertyName != null && !propertyName.equals("")) {
			return new TypedProperty(propertyName, loadedValue, loadedDefaultValue);
		} else {			// No value attribute set.
			return new TypedProperty(null, loadedValue, loadedDefaultValue);
		}
		
	}
	
	
	private static Object getTypedValue(InjectionPoint ip, Class<?> typedClass) {
		
		ConfigTyped configTypedAnn = ip.getAnnotated().getAnnotation(ConfigTyped.class);
		
		String propertyName = configTypedAnn.value();
		RuntimeCheckType runtimeCheckAnn = configTypedAnn.runtimeCheck();
		
		String foundPropertyValue = null;

		if(propertyName != null && !propertyName.equals("")) {
			loggerVerbose.log("VALUE LOADER TYPED -> for type: " + typedClass.getSimpleName());
			foundPropertyValue = LoaderProperties.findPropertyValue(propertyName, runtimeCheckAnn);
		} else {
			loggerVerbose.log("VALUE LOADER TYPED -> no value attribute set.");
			return null;
		}
		
		TypedOperationType type = TypedOperationType.createPropertyType(propertyName);
		
		return parseTyped(ip, type, foundPropertyValue, typedClass);
	}
	
	
	
	private static Object getTypedDefaultValue(InjectionPoint ip, Class<?> typedClass) {
		
		ConfigTyped configJsonAnn = ip.getAnnotated().getAnnotation(ConfigTyped.class);
		
		String stringDefaultValue = configJsonAnn.defaultValue();
		String propertyDefaultValue = configJsonAnn.defaultValueProperty();
		
		TypedOperationType type = null;
		
		if(stringDefaultValue != null && !stringDefaultValue.equals("")) {
			type = TypedOperationType.createDefaultValueType(stringDefaultValue);
		} else if(propertyDefaultValue != null && !propertyDefaultValue.equals("")) {
			type = TypedOperationType.createPropertyDefaultValueType(propertyDefaultValue);
		} else {
			return null;
		}
		
		return LoaderTyped.loadTypedDefaultValue(ip, type, typedClass);
	}
	
	
	private static Object loadTypedDefaultValue(InjectionPoint ip, TypedOperationType type, Class<?> typedClass) {
		loggerVerbose.log("DEFULT VALUE LOADER TYPED -> for: " + type.getValueType().getValue() + ", type: " + typedClass.getSimpleName());
		
		ConfigTyped configTypedAnn = ip.getAnnotated().getAnnotation(ConfigTyped.class);
		RuntimeCheckType runtimeCheckAnn = configTypedAnn.runtimeCheck();
		
		TypedOperationTypeValue typeValue = type.getValueType();
		String name = type.getAttributeValue();
		
		loggerVerbose.log("DEFULT VALUE LOADER TYPED -> propertyKey: " + name);
		
		String loadedDefaultValue = null;
		
		if(typeValue == TypedOperationTypeValue.DEFAULT_VALUE_STRING) {
			
			if(configTypedAnn.defaultValue() != null && !configTypedAnn.defaultValue().equals("")) {
				
				loadedDefaultValue = configTypedAnn.defaultValue();
				
				if(typedClass != String.class) {
					loggerVerbose.log("DEFULT VALUE LOADER TYPED -> for type: " + typedClass.getSimpleName() + ", to parse: " + loadedDefaultValue); 
					return parseTyped(ip, type, loadedDefaultValue, typedClass);
				} else {
					return loadedDefaultValue;
				}
			}
			
		} else if(typeValue == TypedOperationTypeValue.DEFAULT_VALUE_PROPERTY) {
			// if property value and runtime check true - dont reload, value will. Otherwise use annotation's runtime check
			if ((configTypedAnn.value() != null && !configTypedAnn.value().equals("")) &&
					ConfigUtils.runtimeTrueReloadOrPropertiesOtherwise(settings.isRuntimeCheck(), runtimeCheckAnn) == true) {
				loggerVerbose.log("DefVal typed - properties value true and runtime true - no reloading, value does this");
				loadedDefaultValue = LoaderProperties.findPropertyValue(name, RuntimeCheckType.NO);
			} else {
				loggerVerbose.log("DefVal typed - no properties value or runtime false, uses annotation runtime");
				loadedDefaultValue = LoaderProperties.findPropertyValue(name, runtimeCheckAnn);
			}
			
			if(typedClass != String.class) {
				loggerVerbose.log("DEFULT VALUE LOADER TYPED -> for type: " + typedClass.getSimpleName() + ", to parse: " + loadedDefaultValue); 
				return parseTyped(ip, type, loadedDefaultValue, typedClass);
			} else {
				return loadedDefaultValue;
			}
			
		}
		
		loggerVerbose.log("DEFULT VALUE LOADER TYPED -> after loading, class needed: " + typedClass.getSimpleName() + ", loaded value: " + loadedDefaultValue+ ", class: " + (loadedDefaultValue == null? "null": loadedDefaultValue.getClass()));
		
		return loadedDefaultValue;
	}
	
	
	
	private static Object parseTyped(InjectionPoint ip, TypedOperationType type, String foundPropertyValue, Class<?> typedClass) {
		loggerVerbose.log("TYPED VALUE PARSER -> for type: " + typedClass.getSimpleName()); 
		
		try {
			
			if(typedClass == String.class) {
				return foundPropertyValue;
			} else if(typedClass == Double.class) {
				return Double.valueOf(foundPropertyValue);
			} else if(typedClass == Float.class) {
				return Float.valueOf(foundPropertyValue);
			} else if(typedClass == Long.class) {
				return Long.valueOf(foundPropertyValue);
			} else if(typedClass == Integer.class) {
				return Integer.valueOf(foundPropertyValue);
			} else if(typedClass == Short.class) {
				return Short.valueOf(foundPropertyValue);
			} else if(typedClass == Byte.class) {
				return Byte.valueOf(foundPropertyValue);
			} else if(typedClass == Boolean.class) {
				return Boolean.valueOf(foundPropertyValue);
			} else if(typedClass == Character.class) {
				return foundPropertyValue.charAt(0);
			} else {
				return null;
			}
			
		} catch (Exception e) {
			String excMessage = String.format("Error parsing String '%s' to '%s' class value for a %s loader. %s`s attribute: '%s = %s', class: '%s', field name: '%s'.",
					foundPropertyValue, typedClass.getSimpleName(), type.getValueType().getValue(), type.getAnnotationType(), type.getAttributeType(), type.getAttributeValue(), ip.getMember().getDeclaringClass().getName(), ip.getMember().getName());
			if(settings.isAllowDeploymentWithExceptions() == true) {
				logger.severe("Configurator: " + excMessage);
				return null;
			} else {
				throw new ConfiguratorException(excMessage);
			}
		}
	}
	
	
}
