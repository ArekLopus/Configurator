package configurator.typed;

import configurator.enums.TypedOperationTypeValue;

public class TypedOperationType {
	
	private TypedOperationTypeValue valueType;
	private String attributeType;
	private String attributeValue;
	private String annotationType = "@ConfigTyped";
	private String additionalInfo;
	
	
	private static TypedOperationType jsonPropertyObj;
	
	private static TypedOperationType defaultValueObj;
	private static TypedOperationType defaultValuePropertyObj;
	
	
	public static TypedOperationType createPropertyType(String attributeValue) {
		if(jsonPropertyObj == null)
			jsonPropertyObj = new TypedOperationType(TypedOperationTypeValue.PROPERTY, "value");
		jsonPropertyObj.setAttributeValue(attributeValue);
		return jsonPropertyObj;
	}
	
	
	public static TypedOperationType createDefaultValueType(String attributeValue) {
		if(defaultValueObj == null)
			defaultValueObj = new TypedOperationType(TypedOperationTypeValue.DEFAULT_VALUE_STRING, "defaultValue");
		defaultValueObj.setAttributeValue(attributeValue);
		return defaultValueObj;
	}
	
	public static TypedOperationType createPropertyDefaultValueType(String attributeValue) {
		if(defaultValuePropertyObj == null)
			defaultValuePropertyObj = new TypedOperationType(TypedOperationTypeValue.DEFAULT_VALUE_PROPERTY, "defaultValueProperty");
		defaultValuePropertyObj.setAttributeValue(attributeValue);
		return defaultValuePropertyObj;
	}
	
	
	public TypedOperationType() {}
	
	public TypedOperationType(TypedOperationTypeValue valueType, String attributeType) {
		this.valueType = valueType;
		this.attributeType = attributeType;
	}
	
	public TypedOperationType(TypedOperationTypeValue valueType, String attributeType, String attributeValue) {
		this.valueType = valueType;
		this.attributeType = attributeType;
		this.attributeValue = attributeValue;
	}
	
	
	public TypedOperationTypeValue getValueType() {
		return valueType;
	}
	public void setValueType(TypedOperationTypeValue valueType) {
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
		return "TypedOperationType [valueType=" + valueType + ", attributeType=" + attributeType + ", attributeValue="
				+ attributeValue + "]";
	}
	
}
