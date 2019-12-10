package configurator.enums;

public enum JsonOperationTypeValue {
	
	PROPERTY("property value"),
	FILE("file"),
	URL("URL"),
	CLASS_MEMBER("class member"),
	
	DEFAULT_VALUE_STRING("String default value"),
	DEFAULT_VALUE_PROPERTY("property default value"),
	DEFAULT_VALUE_FILE("file default value"),
	DEFAULT_VALUE_URL("url default value"),
	DEFAULT_VALUE_CLASS_MEMBER("class member default value"),
	
	LOADER_PROPERTIES("properties"),
	LOADER_FILES("files"),
	LOADER_URLS("URLs"),
	LOADER_CLASS_MEMBERS("class members");
	
	
	private String value = "None";
	
	private JsonOperationTypeValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
}
