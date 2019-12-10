package configurator;

import javax.enterprise.inject.Vetoed;

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
 * It is used as a container for the injection of a typed object or JSON.
 * <p>
 * Contains methods to get a value or an optional default value if getting the value fails.
 * <pre>
 * {@literal @Inject}
 * {@literal @ConfigTyped("propDouble")}
 * {@literal TypedProperty<Double> myValue;}
 *  ...
 *  Double myDouble = myValue.getValue();	
 * </pre>
 *
 */

@Vetoed
public class TypedProperty<T> {

	private String propertyName;
	private T value;
	private T defaultValue;
	
	
	public TypedProperty() {}
	
	public TypedProperty(String propertyName, T value) {
		this.propertyName = propertyName;
		this.value = value;
	}
	
	public TypedProperty(String propertyName, T value, T defaultValue) {
		this.propertyName = propertyName;
		this.value = value;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Depending on the attribute used, returns a property name for properties, a file path for files, a URL address for URLs, and a field or method name for class members.
	 */
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	/**
	 * Returns injected value (or null if any error).
	 */
	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}
	
	/**
	 * Returns injected default value (or null if not set or any error).
	 */
	public T getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * If a value is not null returns it otherwise returns a default value. 
	 */
	public T getValueOrDefaultValue() {
		if(value != null)
			return value;
		else
			return defaultValue;
	}
	
	/**
	 * If a value is null returns the argument of the method.
	 */
	public T getValueOr(T orValue) {
		if (value != null)
			return value;
		else
			return orValue;
	}
	
	/**
	 * If a value is null and a default value is null returns the argument of the method. Useful when getting a default value also may fail.
	 */
	public T getValueOrDefaultValueOr(T orValue) {
		if(value != null)
			return value;
		else if (value == null && defaultValue != null)
			return defaultValue;
		else
			return orValue;
	}
	
	
	@Override
	public String toString() {
		
		String clazz = value == null ? "value is null" : value.getClass().getName();
		String clazzDefVal = defaultValue == null ? "defaultValue is null" : defaultValue.getClass().getName();
		return "\nTypedProperty name:\t" + propertyName
				+ "\nvalue:\t\t\t" + String.valueOf(value)
				+ "\nvalue class:\t\t" + clazz
				+ "\ndefaultValue:\t\t" + defaultValue
				+ "\ndefaultValue class:\t" + clazzDefVal;
	}
	
}