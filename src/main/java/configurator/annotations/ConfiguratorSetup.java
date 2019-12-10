package configurator.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/*    Copyright 2019 Arkadiusz Lopuszynski 
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 


/**
 * The @ConfiguratorSetup annotation is used to configure paths and settings of the Configurator.
 * <p>
 * The @ConfiguratorSetup annotation sets paths to loads properties from files and to preload JSON.
 * <p>
 * The @ConfiguratorSetup annotation can be used many times, paths are summing up, but if the attributes 'runtimeCheck' and 'allowDeploymentWithExceptions' are set in many places the result is unpredictable. Depends on which annotation is discovered last.
 * <p>
 * For JSON, preload also checks if the JSON structure is valid.
 * <p>
 * To preload URL paths a different application must be used. The Configurator tries to download and verify values for the @ConfiguratorSetup annotation at the deployment time and it can not be done because at this point the current application is not ready to serve responses to requests.
 * <p>
 * Class memebers allow to set values or default values from fields or methods. To preload them, they must be placed in the same class where the @ConfiguratorSetup annotation is located.
 * <p>
 * Difference between property paths and jsonPropertyFilePaths is that if JSON is put in the property paths it is always reloaded and parsed and is not put in the JSON internal storage.
 * If you need JSON from properties use the latter option.
 * <p>
 * 
 * <p>
 * There is an information in logs after deployment for each @ConfguratorSetup annotation that shows settings and what was found and loaded.
 * 	<pre>
 *	 --------------------------------------------------------------------------
 *	 Configurator @ConfiguratorSetup from class: configurator.json.test.TestSetup
 *	{@literal @ConfiguratorSetup runtimeCheck: false, allowDeploymentWithExceptions: true }
 *	{@literal @ConfiguratorSetup readTimeout: 1000, connectionTimout: 1000 }
 *	{@literal @ConfiguratorSetup properties paths found: [/config/prop1.properties, /config/prop2.properties] }
 *	{@literal @ConfiguratorSetup json properties paths found: [] }
 *	{@literal @ConfiguratorSetup json files found: [] }
 *	{@literal @ConfiguratorSetup json urls found: [] }
 *	{@literal @ConfiguratorSetup json members found: [jo, ja] }
 *	 --------------------------------------------------------------------------
 *	<pre>
 *
 */

@Documented
@Retention(RUNTIME)
@Target({TYPE})
public @interface ConfiguratorSetup {
	
	/**
	 * Paths to file(s) containing 'key=value' pairs.
	 */
	@Nonbinding String[] value() default "";
	
	/**
	 * Paths to file(s) containing 'key=stringified JSON' pairs. These values are parsed to check the JSON structure.
	 */
	@Nonbinding String[] jsonPropertyFilePaths() default "";
	/**
	 * Paths to file(s) containing JSON saved in a file. Parsed after loading.
	 */
	@Nonbinding String[] jsonFiles() default "";
	/**
	 * URL address(es) returning JSON. Parsed after downloading.
	 */
	@Nonbinding String[] jsonUrls() default "";
	/**
	 * Method(s) or field(s) that return a JSON or a parsable String.
	 * <p>
	 * This is a special case, only makes sense when there is a large JSON which we want to preload.
	 * <p>
     * They must be in the same class as the @ConfiguratorSetup annotation.
     * <p>
     * Class members are put in the internal storage with a key: 'fullyQualifiedClassName.fieldOrMethodName', they must be accessed by using this key:
     * <p>
     * {@literal @ConfigJson(property = 'fullyQualifiedClassName.fieldOrMethodName')}
	 */
	@Nonbinding String[] jsonMembers() default "";
	
	/**
	 * Sets global runtime check setting (true / false).
	 * <p>
	 * May be overridden locally by @Conifg, @ConifgTyped, and @ConifgJson annotations.
	 * <p>
	 * runtimeCheck true makes every injection to be reloaded, false first tries to get a value from the internal storage and then tries to load if it is not available there.
	 */
	@Nonbinding boolean runtimeCheck() default false;
	/**
	 * When false, if any exception is thrown at the deployment time the deployment fails, if true will only log severe logs with errors.
	 */
	@Nonbinding boolean allowDeploymentWithExceptions() default false;
	
	/**
	 * Used for URL connections.
	 */
	@Nonbinding int connectionTimeout() default 1000;
	/**
	 * Used for URL connections.
	 */
	@Nonbinding int readTimeout() default 1000;
	
}
