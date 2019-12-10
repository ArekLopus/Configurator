package configurator.enums;

public enum TypedOperationTypeValue {
	
	PROPERTY("property value"),
	
	DEFAULT_VALUE_STRING("default value"),
	DEFAULT_VALUE_PROPERTY("property default value");
	
	
	private String value = "None";
	
	private TypedOperationTypeValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
}
