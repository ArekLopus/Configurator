package configurator.json;

import configurator.enums.JsonOperationTypeValue;

public class JsonOperationType {
	
	private JsonOperationTypeValue valueType;
	private String attributeType;
	private String attributeValue;
	private String annotationType = "@ConfigJson";
	private String additionalInfo;
	
	private static JsonOperationType jsonPropertyObj;
	private static JsonOperationType jsonFilePathObj;
	private static JsonOperationType jsonUrlObj;
	private static JsonOperationType jsonClassMemberObj;
	
	private static JsonOperationType defaultValueObj;
	private static JsonOperationType defaultValuePropertyObj;
	private static JsonOperationType defaultValueFileObj;
	private static JsonOperationType defaultValueUrlObj;
	private static JsonOperationType defaultValueClassMemberObj;
	
	private static JsonOperationType jsonPropertiesLoaderObj;
	private static JsonOperationType jsonFilesLoaderObj;
	private static JsonOperationType jsonUrlsLoaderObj;
	private static JsonOperationType jsonMembersLoaderObj;
	
	
	public static JsonOperationType createPropertyType(String attributeValue) {
		if(jsonPropertyObj == null)
			jsonPropertyObj = new JsonOperationType(JsonOperationTypeValue.PROPERTY, "property");
		jsonPropertyObj.setAttributeValue(attributeValue);
		return jsonPropertyObj;
	}
		
	public static JsonOperationType createFilePathType(String attributeValue) {
		if(jsonFilePathObj == null)
			jsonFilePathObj = new JsonOperationType(JsonOperationTypeValue.FILE, "filePath");
		jsonFilePathObj.setAttributeValue(attributeValue);
		return jsonFilePathObj;
	}
	
	public static JsonOperationType createUrlType(String attributeValue) {
		if(jsonUrlObj == null)
			jsonUrlObj = new JsonOperationType(JsonOperationTypeValue.URL, "url");
		jsonUrlObj.setAttributeValue(attributeValue);
		return jsonUrlObj;
	}
	
	public static JsonOperationType createClassMemberType(String attributeValue) {
		if(jsonClassMemberObj == null)
			jsonClassMemberObj = new JsonOperationType(JsonOperationTypeValue.CLASS_MEMBER, "classMember");
		jsonClassMemberObj.setAttributeValue(attributeValue);
		return jsonClassMemberObj;
	}
	
	
	public static JsonOperationType createDefaultValueType(String attributeValue) {
		if(defaultValueObj == null)
			defaultValueObj = new JsonOperationType(JsonOperationTypeValue.DEFAULT_VALUE_STRING, "defaultValue");
		defaultValueObj.setAttributeValue(attributeValue);
		return defaultValueObj;
	}
	
	public static JsonOperationType createPropertyDefaultValueType(String attributeValue) {
		if(defaultValuePropertyObj == null)
			defaultValuePropertyObj = new JsonOperationType(JsonOperationTypeValue.DEFAULT_VALUE_PROPERTY, "defaultValueProperty");
		defaultValuePropertyObj.setAttributeValue(attributeValue);
		return defaultValuePropertyObj;
	}
	
	public static JsonOperationType createFileDefaultValueType(String attributeValue) {
		if(defaultValueFileObj == null)
			defaultValueFileObj = new JsonOperationType(JsonOperationTypeValue.DEFAULT_VALUE_FILE, "defaultValueFile");
		defaultValueFileObj.setAttributeValue(attributeValue);
		return defaultValueFileObj;
	}
	
	public static JsonOperationType createUrlDefaultValueType(String attributeValue) {
		if(defaultValueUrlObj == null)
			defaultValueUrlObj = new JsonOperationType(JsonOperationTypeValue.DEFAULT_VALUE_URL, "defaultValueUrl");
		defaultValueUrlObj.setAttributeValue(attributeValue);
		return defaultValueUrlObj;
	}
	
	public static JsonOperationType createClassMemberDefaultValueType(String attributeValue) {
		if(defaultValueClassMemberObj == null)
			defaultValueClassMemberObj = new JsonOperationType(JsonOperationTypeValue.DEFAULT_VALUE_CLASS_MEMBER, "defaultValueIsClassMember");
		defaultValueClassMemberObj.setAttributeValue(attributeValue);
		return defaultValueClassMemberObj;
	}
	
	
	public static JsonOperationType createJsonPropertiesLoaderType(String attributeValue, String propertyName) {
		if(jsonPropertiesLoaderObj == null)
			jsonPropertiesLoaderObj = new JsonOperationType(JsonOperationTypeValue.LOADER_PROPERTIES, "jsonPropertyFilePaths");
		jsonPropertiesLoaderObj.setAttributeValue(attributeValue);
		jsonPropertiesLoaderObj.setAdditionalInfo(propertyName);
		return jsonPropertiesLoaderObj;
	}
	
	public static JsonOperationType createJsonFilesLoaderType(String attributeValue) {
		if(jsonFilesLoaderObj == null)
			jsonFilesLoaderObj = new JsonOperationType(JsonOperationTypeValue.LOADER_FILES, "jsonFiles");
		jsonFilesLoaderObj.setAttributeValue(attributeValue);
		return jsonFilesLoaderObj;
	}
	
	public static JsonOperationType createJsonUrlsLoaderType(String attributeValue) {
		if(jsonUrlsLoaderObj == null)
			jsonUrlsLoaderObj = new JsonOperationType(JsonOperationTypeValue.LOADER_URLS, "jsonUrls");
		jsonUrlsLoaderObj.setAttributeValue(attributeValue);
		return jsonUrlsLoaderObj;
	}
	
	public static JsonOperationType createJsonMembersLoaderType(String attributeValue) {
		if(jsonMembersLoaderObj == null)
			jsonMembersLoaderObj = new JsonOperationType(JsonOperationTypeValue.LOADER_CLASS_MEMBERS, "jsonMembers");
		jsonMembersLoaderObj.setAttributeValue(attributeValue);
		return jsonMembersLoaderObj;
	}
	
	
	public JsonOperationType() {}
	
	public JsonOperationType(JsonOperationTypeValue valueType, String attributeType) {
		this.valueType = valueType;
		this.attributeType = attributeType;
	}
	
	public JsonOperationType(JsonOperationTypeValue valueType, String attributeType, String attributeValue) {
		this.valueType = valueType;
		this.attributeType = attributeType;
		this.attributeValue = attributeValue;
	}
	
	
	public JsonOperationTypeValue getValueType() {
		return valueType;
	}
	public void setValueType(JsonOperationTypeValue valueType) {
		this.valueType = valueType;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	public String getAnnotationType() {
		return annotationType;
	}
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	@Override
	public String toString() {
		return "JsonOperationType [valueType=" + valueType + ", attributeType=" + attributeType + ", attributeValue="
				+ attributeValue + "]";
	}
	
}
