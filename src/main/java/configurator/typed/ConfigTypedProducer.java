package configurator.typed;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.json.JsonArray;
import javax.json.JsonObject;

import configurator.ConfiguratorSettings;
import configurator.TypedProperty;
import configurator.annotations.ConfigJson;
import configurator.annotations.ConfigTyped;
import configurator.json.LoaderJson;
import configurator.utils.VerboseLogger;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigTypedProducer {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static Logger logger = settings.getLogger();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	@Produces
	public <T> TypedProperty<T> produceTyped(InjectionPoint ip) {
		
		Annotated annotated = ip.getAnnotated();
		ConfigTyped configTypedAnn = annotated.getAnnotation(ConfigTyped.class);
		ConfigJson configJsonAnn = annotated.getAnnotation(ConfigJson.class);
		
		
		if(configJsonAnn != null) {
			loggerVerbose.log(" ------------------------------------------------------------------------------- ");
			
			ParameterizedType parameterizedType = null;
			// Checks if there is a generic type, if no returns String version
			try {
				parameterizedType = (ParameterizedType) ip.getType();
			} catch (Exception e) {
				logger.severe("Configurator: No generic parameter provided, can not cast, returning String version. Class: '" + ip.getMember().getDeclaringClass().getName() + "', field: '" + ip.getMember().getName() + "'.");
				return LoaderJson.loadJson(ip, String.class);
			}
			
			Type type = parameterizedType.getActualTypeArguments()[0];
			
			if(type == JsonObject.class) {
				return LoaderJson.loadJson(ip, JsonObject.class);
			} else if(type == JsonArray.class) {
				return LoaderJson.loadJson(ip, JsonArray.class);
			} else if(type == String.class) {
				return LoaderJson.loadJson(ip, String.class);
			}  
			
		}
		
		
		if(configTypedAnn != null) {
			loggerVerbose.log(" ------------------------------------------------------------------------------- ");
			
			ParameterizedType parameterizedType = null;
			// Checks if there is a generic type, if no returns String version
			try {
				parameterizedType = (ParameterizedType) ip.getType();
			} catch (Exception e) {
				logger.severe("Configurator: No generic parameter provided, can not cast, returning String version. Class: '" + ip.getMember().getDeclaringClass().getName() + "', field: '" + ip.getMember().getName() + "'.");
				return LoaderTyped.loadTyped(ip, String.class);
			}
			
			Class<?> type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
			
			return LoaderTyped.loadTyped(ip, type);
		}
		
		
		return new TypedProperty("No @ConfigXXX annotation set, could not find any values", null, null);
	}
	
	
}