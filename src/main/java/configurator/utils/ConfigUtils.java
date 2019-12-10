package configurator.utils;

import configurator.ConfiguratorSettings;
import configurator.enums.RuntimeCheckType;

public class ConfigUtils {
	
	private static ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
	private static VerboseLogger loggerVerbose = settings.getVerboseLogger();
	

	// Checks whether always reload or use properties if available.
	// Checks @ConfiguratorSetup's runtime check and annotations' runtime check. Annotation takes precedence.
	public static boolean runtimeTrueReloadOrPropertiesOtherwise(boolean globalRuntimeCheck, RuntimeCheckType annotationRuntimeCheck) {
		
		if((globalRuntimeCheck == true && annotationRuntimeCheck == RuntimeCheckType.YES)
				|| (globalRuntimeCheck == false && annotationRuntimeCheck == RuntimeCheckType.YES)
				|| (globalRuntimeCheck == true && annotationRuntimeCheck == RuntimeCheckType.USE_GLOBAL)) {
			loggerVerbose.log("RUNTIME CHECK - ALWAYS RELOADING. Global Runtime = " + settings.isRuntimeCheck() + ", Annotation Runtime = " + annotationRuntimeCheck);
			return true;
		} else {
			loggerVerbose.log("RUNTIME CHECK - PROPERTIES, NO RELOADING NEEDED. Global Runtime = " + settings.isRuntimeCheck() + ", Annotation Runtime = " + annotationRuntimeCheck);
			return false;
		}
	}
	
	
	// Cuts Strings to a given length
	public static String displayStringOfLength(String str, int length) {
		if(str.length() <= length)
			return str;
		else
			return str.substring(0, length) + " ...";
	}
	
	
}
