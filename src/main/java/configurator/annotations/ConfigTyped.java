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
 * A convenience version, injects properties from the environmental variables, system properties, or property files and automatically 
 * tries to parse loaded String values to the Java data types: Double, Float, Long, Integer, Short, Byte, Character, Boolean.
 * <p>
 * The {@literal @ConfigTyped} annotation and the {@link configurator.TypedProperty} are used for typed objects.
 * <p>
 * The value attribute must be used if there is a defaultValue or defaultValueProperty attribute set.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigTyped("propDouble")}
 * {@literal TypedProperty<Double> myValue;}
 *  ...
 *  Double myDouble = myValue.getValue();	
 * </pre>
 * There are 2 types of a default value for the {@literal @ConfigTyped} annotation.
 * <p>
 * Passed as a String.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigTyped(value = "propDouble", defaultValue = "1.0")}
 * {@literal TypedProperty<Double> myValue;}
 * </pre>
 * <p>
 * Or read from a property.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigTyped(value = "propDouble", defaultValueProperty = "propDoubleDefVal")}
 * {@literal TypedProperty<Double> myValue;}
 * </pre>
 * Default values are not used automatically for the TypedProperty type.
 * <p>
 * You need to check for null or use the getValueOrDefaultValue() method which will return a default value if a value is null.
 * <pre>
 * Double myDouble = myValue.getValue();
 * if (myDouble != null)
 *   // do something
 * else
 *   // myValue.getDefaultValue();
 * ...
 * or
 * Double myDouble = myValue.getValueOrDefaultValue();
 *  
 */

@Documented
@Retention(RUNTIME)
@Target({ FIELD })
public @interface ConfigTyped {
	
	/**
	 * A property name.
	 */
	@Nonbinding	String value();
	
	/**
	 * Sets a default value from a String.
	 */
	@Nonbinding	String defaultValue() default "";
	/**
	 * A default value is read from a property.
	 */
	@Nonbinding	String defaultValueProperty() default "";
	
	/**
	 * Uses or overrides the 'runtimeCheck' attribute on the {@literal @ConfiguratorSetup} annotation.
	 * <p>
	 * The default setting is RuntimeCheckType.USE_GLOBAL which means to use what is set on the {@literal @ConfiguratorSetup} annotation.
	 * You can override this with RuntimeCheckType.YES or RuntimeCheckType.NO.
	 */
	@Nonbinding	RuntimeCheckType runtimeCheck() default RuntimeCheckType.USE_GLOBAL;
	
}