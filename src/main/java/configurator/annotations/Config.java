package configurator.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

import configurator.enums.RuntimeCheckType;

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
 * The simplest version, injects properties from the environmental variables, system properties, or property files as a String.
 * <p>
 * The value attribute must be used if there is a defaultValue attribute set.
 * <pre>
 * {@literal @Inject}
 * {@literal @Config("propertyName")  or  @Config(value = "propertyName") }
 *  String propertyValue;
 * </pre>
 * The defaultValue attribute is automatically used when there is no property value with a given name (the value is null).
 * <pre>
 * 
 * {@literal @Inject}
 * {@literal @Config(value = "badName", defaultValue = "No value found.") }
 *  String propertyValue;
 * </pre>
 */

@Documented
@Retention(RUNTIME)
@Target({ FIELD })
public @interface Config {
	
	/**
	 * A property name.
	 */
	@Nonbinding	String value();
	/**
	 * If set, it is automatically used when the value of a property is null.
	 */
	@Nonbinding	String defaultValue() default "";
	
	/**
	 * Uses or overrides the 'runtimeCheck' attribute on the {@literal @ConfiguratorSetup} annotation.
	 * <p>
	 * The default setting is RuntimeCheckType.USE_GLOBAL which means to use what is set on the {@literal @ConfiguratorSetup} annotation.
	 * You can override this with RuntimeCheckType.YES or RuntimeCheckType.NO.
	 */
	@Nonbinding	RuntimeCheckType runtimeCheck() default RuntimeCheckType.USE_GLOBAL;
	
}
