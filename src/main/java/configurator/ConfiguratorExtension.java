package configurator;

import java.util.Arrays;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import configurator.annotations.ConfiguratorSetup;
import configurator.json.LoaderJsonSetup;
import configurator.typed.LoaderProperties;
import configurator.utils.VerboseLogger;


public class ConfiguratorExtension implements Extension {
	
	
	protected <T> void processAnnotatedType(@Observes @WithAnnotations(ConfiguratorSetup.class) ProcessAnnotatedType<T> pat, BeanManager bm) {
		
		AnnotatedType<T> at = pat.getAnnotatedType();
		ConfiguratorSetup annotation = at.getAnnotation(ConfiguratorSetup.class);
		
		if(annotation != null) {
			
			ConfiguratorSettings settings = ConfiguratorSettings.getInstance();
			Class<T> javaClass = at.getJavaClass();
			
			boolean runtimeCheck = annotation.runtimeCheck();
	        boolean allowDeploymentWithExceptions = annotation.allowDeploymentWithExceptions();
			
	        int readTimeout = annotation.readTimeout();
	        int connectionTimeout = annotation.connectionTimeout();
			
	        settings.setRuntimeCheck(runtimeCheck);
			settings.setAllowDeploymentWithExceptions(allowDeploymentWithExceptions);
			
			settings.setReadTimout(readTimeout);
			settings.setConnectionTimout(connectionTimeout);
	        
	        HashSet<String> propertiesPaths = new HashSet<String>(Arrays.asList(annotation.value()));
			propertiesPaths.remove("");		// remove default ""
			HashSet<String> jsonPropertiesPaths = new HashSet<String>(Arrays.asList(annotation.jsonPropertyFilePaths()));
			jsonPropertiesPaths.remove("");
			HashSet<String> jsonFiles = new HashSet<String>(Arrays.asList(annotation.jsonFiles()));
			jsonFiles.remove("");
			HashSet<String> jsonUrls = new HashSet<String>(Arrays.asList(annotation.jsonUrls()));
			jsonUrls.remove("");
			HashSet<String> jsonMembers = new HashSet<String>(Arrays.asList(annotation.jsonMembers()));
			jsonMembers.remove("");
			
			if(propertiesPaths.contains("verbose")) {
				settings.setVerbose(true);
				propertiesPaths.remove("verbose");
			}
			VerboseLogger loggerVerbose = settings.getVerboseLogger();
			
			
			System.out.println("Configurator"
					+ "\n--------------------------------------------------------------------------"
					+ "\nConfigurator @ConfiguratorSetup from class: " + javaClass.getName()
					+ "\n@ConfiguratorSetup runtimeCheck: " + runtimeCheck + ", allowDeploymentWithExceptions: " + allowDeploymentWithExceptions
					+ "\n@ConfiguratorSetup readTimeout: " + readTimeout + ", connectionTimout: " + connectionTimeout
					+ "\n@ConfiguratorSetup properties paths found: " + propertiesPaths
					+ "\n@ConfiguratorSetup json properties paths found: " + jsonPropertiesPaths
					+ "\n@ConfiguratorSetup json files found: " + jsonFiles
					+ "\n@ConfiguratorSetup json urls found: " + jsonUrls
					+ "\n@ConfiguratorSetup json members found: " + jsonMembers
					+ "\n--------------------------------------------------------------------------");
			
			
			LoaderProperties.loadPropertiesFromFile(propertiesPaths);
			LoaderJsonSetup.loadJsonPropertiesFromPaths(jsonPropertiesPaths);
			LoaderJsonSetup.loadJsonFromFiles(jsonFiles);
			LoaderJsonSetup.loadJsonFromUrls(jsonUrls);
			LoaderJsonSetup.loadJsonFromMembers(jsonMembers, javaClass);
			
			settings.setPropertiesPaths(propertiesPaths);
			settings.setJsonPropertiesPaths(jsonPropertiesPaths);
			settings.setJsonFilePaths(jsonFiles);
			settings.setJsonUrls(jsonUrls);
			settings.setJsonMembers(javaClass, jsonMembers);
			
			loggerVerbose.log("jsonProperties -> size: " + settings.getJsonProperties().size() + ", keys: " + settings.getJsonProperties().keySet());
			
		}
		
    }
	
}
