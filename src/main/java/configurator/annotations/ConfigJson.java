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
 * Injects JSON from the environmental variables, system properties, property files, JSON files, URLs, and class members.
 * <p>
 * The {@literal @ConfgJson} annotation and the {@link configurator.TypedProperty} are used for for JSON objects, arrays, and as Strings.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(property = "propJsonArray")}
 * {@literal TypedProperty<JsonArray> json;}
 *  ...
 *  JsonArray ja = json.getValue();
 * 
 * 
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json")}
 * {@literal TypedProperty<JsonObject> json;}
 *  ...
 *  JsonObject jo = json.getValue();
 * 
 * 
 * {@literal @Inject}
 * {@literal @ConfigJson(url = "http://host:port/resources/json")}
 * {@literal TypedProperty<String> json;}
 *  ...
 *  String jos = json.getValue();
 * 
 * 
 * {@literal @Inject}
 * {@literal @ConfigJson(classMember = "methodOrFieldNameReturningJSON")}
 * {@literal TypedProperty<JsonObject> json;}
 *  ...
 *  JsonObject jo = json.getValue();
 * </pre>
 * 
 * 
 * There are 5 types of a default value for the {@literal @ConfigJson} annotation.
 * <p>
 * Passed as a String.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json", defaultValue = "{\"error\" : \"There was an error loading the value.\"}")}
 * {@literal TypedProperty<JsonObject> json;}
 * </pre>
 * <p>
 * From a property.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json", defaultValueProperty = "propJsonObjectDefVal")}
 * {@literal TypedProperty<JsonObject> json;}
 * </pre>
 * From a JSON file.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json", defaultValueFile = "/config/configObjectDefVal.json")}
 * {@literal TypedProperty<JsonObject> json;}
 * </pre>
 * From a URL.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json", defaultValueUrl = "http://host:port/resources/jsonDefVal")}
 * {@literal TypedProperty<JsonObject> json;}
 * </pre>
 * From a class member.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigJson(filePath = "/config/configObject.json", defaultValueIsClassMember = "methodOrFieldName")}
 * {@literal TypedProperty<JsonObject> json;}
 * </pre>
 * Default values are not used automatically for the TypedProperty type.
 * <p>
 * You need to check for null or use the getValueOrDefaultValue() method which will return a default value if a value is null.
 * <pre>
 * JsonObject myJson = json.getValue();
 * if (myJson != null)
 *   // do something
 * else
 *   // myValue.getDefaultValue();
 * ...
 * or
 * JsonObject myJson = json.getValueOrDefaultValue();
 */

@Documented
@Retention(RUNTIME)
@Target({ FIELD })
public @interface ConfigJson {
	
	/**
	 * A property name for a value.
	 */
	@Nonbinding	String property() default "";
	/**
	 * Path to a JSON file for a value.
	 */
	@Nonbinding	String filePath() default "";
	/**
	 * A URL address to download JSON for a value.
	 */
	@Nonbinding	String url() default "";
	/**
	 * Name of a field or method for a value.
	 */
	@Nonbinding	String classMember() default "";
	
	/**
	 * Sets a default value from a String.
	 */
	@Nonbinding	String defaultValue() default "";
	/**
	 * A default value is read from a property.
	 */
	@Nonbinding	String defaultValueProperty() default "";
	/**
	 * Path to a JSON file for a default value.
	 */
	@Nonbinding	String defaultValueFile() default "";
	/**
	 * A URL address to download JSON for a default value.
	 */
	@Nonbinding	String defaultValueUrl() default "";
	/**
	 * Name of a field or method for a default value.
	 */
	@Nonbinding	String defaultValueIsClassMember() default "";
	
	/**
	 * Uses or overrides the 'runtimeCheck' attribute on the {@literal @ConfiguratorSetup} annotation.
	 * <p>
	 * The default setting is RuntimeCheckType.USE_GLOBAL which means to use what is set on the {@literal @ConfiguratorSetup} annotation.
	 * You can override this with RuntimeCheckType.YES or RuntimeCheckType.NO.
	 */
	@Nonbinding	RuntimeCheckType runtimeCheck() default RuntimeCheckType.USE_GLOBAL;
	
}