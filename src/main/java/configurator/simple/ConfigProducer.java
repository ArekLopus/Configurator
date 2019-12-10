package configurator.simple;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import configurator.ConfiguratorSettings;
import configurator.annotations.Config;
import configurator.enums.RuntimeCheckType;
import configurator.typed.LoaderProperties;
import configurator.utils.VerboseLogger;

public class ConfigProducer {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	
	
	@Produces
	public String produceConfig(InjectionPoint ip) {
		
		Config confAnn = ip.getAnnotated().getAnnotation(Config.class);
		
		if(confAnn != null) {
			
			String name = confAnn.value();
			String defValue = confAnn.defaultValue();
			RuntimeCheckType annRuntimeCheck = confAnn.runtimeCheck();
			
			String foundPropertyVal = LoaderProperties.findPropertyValue(name, annRuntimeCheck);
			
			if(foundPropertyVal == null || foundPropertyVal.equals("")) {
				if(defValue != null && !defValue.equals("")) {
					loggerVerbose.log("@Config producer -> property value not found, returning def val: " + defValue);
					return defValue;
				} else {
					loggerVerbose.log("@Config producer -> property value not found, def val not found");
					return null;
				}
			} else {
				return foundPropertyVal;
			}
			
		}
		
		return null;
	}
	
}
