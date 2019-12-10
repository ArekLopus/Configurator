
# Java EE / Jakarta EE Applications Configurator

 - Allows to inject Strings, typed objects and JSON objects or arrays.
   - The [`@ConfiguratorSetup` ](#config-setup) annotation is used to configure paths to properties and values and Configurator settings.
   - The `@Inject` and [`@Config`](#config-config-ann) annotations are used to inject Strings.
   - The `@Inject` and [`@ConfigTyped`](#config-configtyped-ann) annotations and [`TypedProperty<TYPE>`](#config-typed-property)
   are used to inject typed objects. This version is for a convenience. It automatically tries to parse
   loaded String values to the Java data types: Double, Float, Long, Integer, Short, Byte, Character, Boolean.
   - The `@Inject` and [`@ConfigJson`](#config-configjson-ann) annotations and [`TypedProperty<TYPE>`](#config-typed-property) are used to inject JSON as objects or Strings.
   - <a target="_blank" rel="noopener noreferrer" href="https://areklopus.github.io/Configurator/javadocs/">javadocs</a>.
   - [Examples](#config-examples)
 - Allows to read values from the environmental variables, system properties, property files.
   - for JSON also from files, URLs, and class members.
 - Allows to set a default value if getting a value fails.
   - for Strings - String.
   - for typed objects - String, property value.
   - for JSON - String, property value, file, URL, class member.
 - Allows runtime reloads to change settings at runtime time.
   - Allows to set runtime globally (@ConfguratorSetup annotation) or locally (@ConifgXXX annotations).
   - Local setting overrides global setting.
 - Allows to preload values at the deployment time for faster access (at runtime they are accessed from the internal storage).
 - Uses internal storage for faster access to values and default values (they are loaded first time and then accessed locally).
   - If runtime check is false it first will try to load a value from the internal storage and if not found then from the source.
 - Errors show the exact point where a problem happened.
	```
	3 different errors:
	SEVERE: Configurator: Error loading JSON, exception: '/config/configArrayBadName.json (The system cannot find the file specified)', for the file loader. @ConfigJson`s attribute: 'filePath = /config/configArrayBadName.json', class: 'confugurator.json.test.Test', field name: 'test'.
	SEVERE: Configurator: Error downloading JSON, response status code: '404', exception: 'http://localhost:8080/test/json/badUrl', for the URL loader. @ConfigJson`s attribute: 'url = http://localhost:8080/test/json/badUrl', class: 'confugurator.json.test.Test', field name: 'test'.
	SEVERE: Configurator: Can not find the field or method named: 'badFieldName' in the class: 'configurator.test.resources.json.memeber.JO_FieldBad' for the class member loader. @ConfigJson`s attribute: 'classMember = badFieldName', class: 'confugurator.json.test.Test', field name: 'test'.
	```
 - Class members as a source allow to return a value from a field or method.  
  Method returning a JSON value from DB, file, or REST call after some computation and runtime reloading gives a quite powerful
  abitities to configure an application during runtime.


### Instalation
This library is a CDI extension, it requires at least **Java EE 7.0** Application Server.

To use it, copy the target/configurator.jar file or build it from the source code using `mvn package` command.

Or use `mvn install` command to add it to the local repository and then use it as a dependency:
```
  <dependency>
      <groupId>config</groupId>
      <artifactId>configurator</artifactId>
      <version>1.0.0</version>
  </dependency>
```

After adding it to the project this extension is automatically discovered and ready to work.


### General rules
- The order of the search for values is: the environmental variables, system properties, all other sources (property files, preloaded JSON - they are loaded at the deployment time).
- The environmental variables and system properties are always dynamic, they are not stored and are read for every request.
- Property files must be set on the @ConfiguratorSetup annotation to be accessible, other source types can be also set on a particular @Config annotation.
- If no type parameter is set for the `TypedProperty<TYPE>` a value injected is the String type.
- All JSON operations also allow String version of JSON.
  - You can inject JSON as a String using the `TypedProperty<String>` and the `@ConfgJson` annotation.
  - For a class member, a field or method also may return a stringified version of JSON, it will be automatically parsed.
- For JSON, Strings are always parsed to check if the structure is valid.
- For class members, a field or method may have any access modifier and may be static.
- For class members, if a field and method have the same name the method will be loaded.
- If there is more than one of the value or default value attribues set, the first one found is taken:
  - For values:
    - property
    - filePath
    - url
    - classMember
  - For default values:
    - defaultValue
    - defaultValuePoperty
    - defaultValueFile
    - defaultValueUrl
    - defaultValueIsClassMember
- Values and default values are stored in the in the internal storage after a first load.
- To avoid name clashes keys to store in the internal storage are:
  - For values:
    - property – property name
    - filePath - file name
    - url - url address
    - classMember - fullyQualifiedClassName.memeberName
  - For default values:
    - defaultValue – a value is also the key.
    - defaultValuePoperty – property name
    - defaultValueFile – file path
    - defaultValueUrl – url address
    - defaultValueIsClassMember - fullyQualifiedClassName.memeberName


### <a name="config-setup"></a>The `@ConfiguratorSetup` annotation for startup configuring.
 - The @ConfiguratorSetup annotation is used to configure settings and paths to values of the Configurator.
 - The @ConfiguratorSetup annotation sets paths to load properties from files and to preload JSON.
 - The @ConfiguratorSetup annotation can be used many times, paths are summing up, but if the attributes
 'runtimeCheck' and 'allowDeploymentWithExceptions' are set in many places the result is unpredictable.
 Depends on which annotation is discovered last.
 - If there is no the @ConfiguratorSetup annotation the default settings are used (see the attributes below).
 - Allows to preload values to be ready to inject after the deployment (except for the environmental variables and system properties).
 - For JSON, preload also checks if the JSON structure is ok.
 - To preload URL paths a different application must be used. Configurator tries to download and verify
   values for the @ConfiguratorSetup annotation at the deployment time and it can not be done because
   at this point the current application is not ready to serve responses to requests. See [GOTCHAS](#config-gotchas) to how to overcome this.
 - Class memebers allow to set values or default values from fields or methods. To preload them, they
   must be placed in the same class where the @ConfiguratorSetup annotation is located.


 - The @ConfiguratorSetup annotation attributes:
   - **value** - paths to file(s) containing 'key=value' pairs (the value attribute must be used if there are other attributes set).
     ```
     @ConfguratorSetup("/config/props.properties")  or  @ConfguratorSetup(value = "/config/props.properties")
     @ConfguratorSetup({"/config/props1.properties", "/config/props2.properties"})
     ```
  
   - **jsonPropertyFilePaths** - paths to file(s) containing 'key=stringified JSON' pairs. These values are parsed to check the JSON structure.
     ```
     @ConfguratorSetup(jsonPropertyFilePaths = "/config/jsonProps.properties")
     @ConfguratorSetup(jsonPropertyFilePaths = {"/config/jsonProps1.properties", "/config/jsonProps2.properties"})
     ```
   - **jsonFiles** - Paths to file(s) containing JSON saved in a file. Parsed after loading.
     ```
     @ConfguratorSetup(jsonFiles = "/config/myJsonObject.json")
     @ConfguratorSetup(jsonFiles = {"/config/myJsonObject1.json", "/config/myJsonObject2.json"})
     ```
   - **jsonUrls** - URL address(es) returning JSON. Parsed after downloading.
     ```
     @ConfguratorSetup(jsonUrls = "http://localhost/myApp/resources/myJsonObject")
     @ConfguratorSetup(jsonUrls = {"http://localhost/myApp/resources/myJsonObject1", "http://localhost/myApp/resources/myJsonObject2"})
     ```
   - **jsonMembers**	- method(s) or field(s) that return a JSON or a parsable String.
     - This is a special case, only makes sense when there is a large JSON which we want to preload.
     - They must be in the same class as the @ConfiguratorSetup annotation.
     - Class members are put in the internal storage with a key: 'fullyQualifiedClassName.fieldOrMethodName',
       they are accessed by using this key: `@ConfigJson(property = 'fullyQualifiedClassName.fieldOrMethodName')`.
     ```
     @ConfguratorSetup(jsonMembers = "fieldOrMethodName")
     @ConfguratorSetup(jsonMembers = {"fieldOrMethodName1", "fieldOrMethodName2"})
     ```
   - **runtimeCheck** (true / false, default false) - sets global runtime check setting. May be overridden
     locally by the @Conifg, @ConifgTyped, and @ConifgJson annotations. Runtime check true makes every injection to be reloaded, false first tries to get a value from the internal storage and then tries to load if it is not available there.
   - **allowDeploymentWithExceptions** (true / false, default false) - When false, if any exception is thrown at the deployment time the deployment fails, if true will only log severe logs with errors.
	
   - **connectionTimeout** (default 1000 ms) - used for URL connections.
   - **readTimeout** (default 1000 ms) - used for URL connections.

 - Difference between property paths and jsonPropertyFilePaths is that if JSON is put in the property paths
   it is always reloaded and parsed and is not put in the JSON internal storage. If you need JSON from properties use the latter option.

 - There is an information in logs after deployment for each @ConfguratorSetup annotation found that shows settings and what was found and loaded.
 	```
	--------------------------------------------------------------------------
	Configurator @ConfiguratorSetup from class: configurator.json.test.TestSetup
	@ConfiguratorSetup runtimeCheck: false, allowDeploymentWithExceptions: true
	@ConfiguratorSetup readTimeout: 1000, connectionTimout: 1000
	@ConfiguratorSetup properties paths found: [/config/prop1.properties, /config/prop2.properties]
	@ConfiguratorSetup json properties paths found: []
	@ConfiguratorSetup json files found: []
	@ConfiguratorSetup json urls found: []
	@ConfiguratorSetup json members found: [bad, jo, ja, jom, jos]
	--------------------------------------------------------------------------
	```

####  allowDeploymentWithExceptions
  - The CDI runtime at deployment time tries to create all CDI beans and if any value can not be loaded it throws an exception
  and then the whole deployment fails.
  - The `allowDeploymentWithExceptions` attribute allows to install application with not satisfied value injections,
  shows deployment errors in logs, and then you need to be more cautious for null values.
	```
	SEVERE: Configurator: Can not find the field or method named: 'bad' in the class: 'configurator.json.test.TestSetup' for the class members loader. @ConfiguratorSetup`s attribute: 'jsonMembers = bad'.
	```


###  Runtime check
 - Allows runtime reloads to change settings at the runtime.
   - A runtime check may be set globally (@ConfguratorSetup) or locally (@Conifg, @ConifgTyped, @ConifgJson).
   - Local settings override the global setting.
 - The @ConfiguratorSetup annotation uses the attribute `runtimeCheck` (true / false, default false) - true
   makes every injection to be reloaded, false first tries to get a value from the internal storage
   and then tries to load if not available there.
 - The @ConfigXXX annotations use the attribute `runtimeCheck` (JsonRuntimeCheck, default JsonRuntimeCheck.USE_GLOBAL).
 - By default the `runtimeCheck` attribute is set to `false` on @ConfiguratorSetup and `JsonRuntimeCheck.USE_GLOBAL` on the @ConfgXXX annotations
  (which means to use what is set on the @ConfiguratorSetup annotation).
  This setting will load values to the internal storage at the first access and then use it from the internal storage for further requests.
 - Setting the `runtimeCheck` attribute to true on the @ConfiguratorSetup will force to reload requested values every time.
 - Setting `JsonRuntimeCheck.YES` or `JsonRuntimeCheck.NO` on the @ConfgXXX annotations overrides
 what is set on the @ConfiguratorSetup annotation for a prticular injection.


	
#### JsonRuntimeCheck enum
 - By default the @Config, @ConfigTyped, and @ConfigJson annotations use the `JsonRuntimeCheck.USE_GLOBAL`,
 which means to use the @ConfiguratorSetup's runtimeCheck attribute setting.
 - `JsonRuntimeCheck.YES` overrides for the annotation to use runtime check.
 - `JsonRuntimeCheck.NO` overrides for the annotation to not to use runtime check.


### <a name="config-typed-property"></a>The `TypedProperty` class
 - It is used as a container for injection of a typed object or JSON.
 - Contains methods to get a value or an optional default value if getting the value fails.
   - **getPropertyName()** - depending on the attribute used, returns a property name for properties, a file path for files, a URL address for URLs, and a field or method name for class members.
   - **getValue()** - returns the injected value (or null if any error).
   - **getDefaultValue()** - returns the injected default value (if not set or fails to get it, returns null).
   - **getValueOrDefaultValue()** - if the value is not null returns it otherwise returns the default value.
   - **getValueOr(T orValue)** - If the value is null returns the argument of the method.
   - **getValueOrDefaultValueOr(T orValue)** - If a value is null and a default value is null returns the argument of the method. Useful when getting the default value also may fail.
 - Example
   ```
   @Inject
   @ConfgJson(filePath = "/config/myJson.json", defaultValueFile="/config/myDefValJson.json")
   TypedProperty<JsonObject> jsonValue;
   ```


### <a name="config-config-ann"></a>The `@Config` annotation

 - The simplest version, injects properties from the environmental variables, system properties, or property files as a String.
 - The @Confg annotation attributes:
   - **value** - a property name (the value attribute must be used if there is the defaultValue attribute set).
     ```
     @Inject
     @Config("propertyName")  or  @Config(value = "propertyName")
     String propertyValue;
     ```
  
   - **defaultValue** - injected instead of a value when it is null for any reason.
     ```
     @Inject
     @Config(value = "propertyName", defaultValue="Injection of the value failed.")
     String propertyValue;
     ```

   - **runtimeCheck** (default RuntimeCheckType.USE_GLOBAL) - uses or overrides the one used by the @ConfiguratorSetup annotation


### <a name="config-configtyped-ann"></a>The `@ConfgTyped` annotation
 - A convenience version, injects properties from the environmental variables, system properties,
 or property files and automatically tries to parse loaded String values to the Java data types:
 Double, Float, Long, Integer, Short, Byte, Character, Boolean.

 - The @ConfgTyped annotation attributes:
   - **value** - a property name (the value attribute must be used if there is the defaultValue attribute set).
     ```
     @Inject
     @ConfigTyped("propertyName")  or  @ConfigTyped(value = "propertyName")
     TypedProperty<Double> propertyValue;
     ```
  
   - **defaultValue** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigTyped(value = "propertyName", defaultValue="123")
     TypedProperty<Integer> propertyValue;
     ```

   - **defaultValueProperty** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigTyped(value = "propertyName", defaultValueProperty="propertyNameForDefaultValue")
     TypedProperty<String> propertyValue;
     ```

   - **runtimeCheck** (default RuntimeCheckType.USE_GLOBAL) - uses or overrides the one used by the @ConfiguratorSetup annotation


### <a name="config-configjson-ann"></a>The `@ConfgJson` annotation

 - The @ConfgJson annotation attributes
  
   - **property** - a property name from a property file containing a stringified JSON object / array.
     ```
     @Inject
     @ConfigJson(property = "propertyName")
     TypedProperty<JsonObject> jsonValue;
     ```
  
   - **filePath** - a path to a file containing a JSON object / array.
     ```
     @Inject
     @ConfigJson(filePath = "/config/myJson.json")
     TypedProperty<JsonObject> jsonValue;
     ```
  
   - **url** - a url address returning a JSON object / array.
     ```
     @Inject
     @ConfigJson(url = "http://host:port/resources/json")
     TypedProperty<JsonObject> jsonValue;
     ```
  
   - **classMember** - a field or method returning a JSON object / array.
     ```
     @Inject
     @ConfigJson(classMember = "fieldOrMethodName")
     TypedProperty<JsonObject> jsonValue;
     ```
  
   - **defaultValue** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigJson(filePath = "/config/myJson.json", defaultValue="{\"error\" : \"There was an error getting JSON value.\"}")
     TypedProperty<JsonObject> jsonValue;
     ```

   - **defaultValueProperty** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigJson(filePath = "/config/myJson.json", defaultValueProperty="propertyNameForDefaulrValue")
     TypedProperty<JsonObject> jsonValue;
     ```

   - **defaultValueFile** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigJson(filePath = "/config/myJson.json", defaultValueFile="/config/myDefValJson.json")
     TypedProperty<JsonObject> jsonValue;
     ```

   - **defaultValueUrl** - may be used when a value is null for any reason.
     ```
     @Inject
     @ConfigJson(filePath = "/config/myJson.json", defaultValueUrl="http://host:port/resources/jsonDefVal")
     TypedProperty<JsonObject> jsonValue;
     ```

    - **defaultValueIsClassMember** - may be used when a value is null for any reason.
      ```
      @Inject
      @ConfigJson(filePath = "/config/myJson.json", defaultValueIsClassMember="jsonMethod")
      TypedProperty<JsonObject> jsonValue;
      ```

    - **runtimeCheck** (default RuntimeCheckType.USE_GLOBAL) - uses or overrides the one used by the @ConfiguratorSetup annotation




### <a name="config-examples"></a>Examples

Lets assume we have these files located in the 'config' directory.

```
props.properties
	myProp=Property from the file 'prop.properties'
	myPropDefVal=A default value property from the file 'prop.properties'
	propDouble=2.67
	propDoubleDefVal=3.67

jsonProps.properties
	propJsonObject={"propKey1" : "propVal1", "propKey2" : "propVal2"}
	propJsonObjectDefVal={"propKeyDefVal1" : "propValDefVal1", "propKeyDefVal2": "propValDefVal2"}
	propJsonArray=["propVal1","propVal2","propVal3"]
	propJsonArrayDefVal=["propValDefVal1","propValDefVal2","propValDefVal3"]

jsonObject.json
	{ "fileKey1": "fileVal1", "fileKey2": "fileVal2" }

jsonObjectDefVal.json
	{ "fileDefValKey1": "fileDefValVal1", "fileDefValKey2": "fileDefValVal2" }
```

We use the @ConfiguratorSetup annotation to configure the Configurator.
```
@ConfiguratorSetup(
	runtimeCheck = true,
	allowDeploymentWithExceptions = false,
	value = "/config/props.properties",
	jsonPropertyFilePaths = "/config/jsonProps.properties",
	jsonFiles = {"/config/jsonObject.json", "/config/jsonArray.json"}
)
public class ConfigSetup {}

```

Now we can inject our properties.

- The `@Config` annotation injects properties as a String.
  ```
  @Inject
  @Config("myProp")
  String propValue;				// Property from the file 'prop.properties'
  ```  

  - We can set a default value which will be used automatically when a value is not available. 
    ```
    @Inject
    @Config(value = "myProp2", defaultValue = "There was an error getting 'myProp' property.")
    String propValue;				// There was an error getting 'myProp2' property.
    ```  

- The `@ConfgTyped` annotation and the `TypedProperty<TYPE>` type are used for typed objects.
  ```
  @Inject
  @ConfigTyped("propDouble")
  TypedProperty<Double> myValue;
  ...
  Double myDouble = myValue.getValue();				// 2.67
  ```

  - We can set 2 types of a default value for the `@ConfgTyped` annotation.  
    Passed as a String
    ```
    @Inject
    @ConfigTyped(value = "propDouble", defaultValue = "1.0")
    TypedProperty<Double> myValue;
    ...
    Double myDouble = myValue.getDefaultValue();				// 1.0
    ```
    Or read from a property.
    ```
    @Inject
    @ConfigTyped(value = "propDouble", defaultValueProperty = "propDoubleDefVal")
    TypedProperty<Double> myValue;
    ...
    Double myDouble = myValue.getDefaultValue();				// 3.67
    ```
    Default values are not used automatically for the `TypedProperty` type. You need to check for `null` or use the `getValueOrDefaultValue()` method which will return the default value if a value is null.
    ```
    @Inject
    @ConfigTyped(value = "propDouble", defaultValueProperty = "propDoubleDefVal")
    TypedProperty<Double> myValue;
    ...
    Double myDouble = myValue.getValue();
    if (myDouble != null)
        // do something
    else
        // myValue.getDefaultValue();
    ...
    or
    Double myDouble = myValue.getValueOrDefaultValue();
    ```
  
  
- The `@ConfgJson` annotation and the `TypedProperty<TYPE>` type are used for JSON objects, arrays, and as Strings.  
  From property files
  ```
  @Inject
  @ConfigJson(property = "propJsonObject")
  TypedProperty<JsonObject> json;
  ...
  JsonObject jo = json.getValue();
  ```

  ```
  @Inject
  @ConfigJson(property = "propJsonArray")
  TypedProperty<JsonArray> json;
  ...
  JsonArray ja = json.getValue();
  ```

  ```
  @Inject
  @ConfigJson(property = "propJsonObject")
  TypedProperty<String> json;
  ...
  String jos = json.getValue();
  ```
  From JSON files
  ```
  @Inject
  @ConfigJson(filePath = "/config/jsonObject.json")
  TypedProperty<JsonObject> json;
  ...
  JsonObject jo = json.getValue();
  ```
  From URLs
  ```
  @Inject
  @ConfigJson(url = "http://host:port/resources/json")
  TypedProperty<JsonObject> json;
  ...
  JsonObject jo = json.getValue();
  ```
  From class members
  ```
  @Inject
  @ConfigJson(classMember = "getMyJson")
  TypedProperty<JsonObject> json;
  ...
  private JsonObject getMyJson() { ... }
  ...
  JsonObject jo = json.getValue();
  ```
 
  - There are 5 types of a default value for the `@ConfgJson` annotation.  
    Passed as a String
    ```
    @Inject
    @ConfigJson(filePath = "/config/jsonObject.json", defaultValue = "{\"error\" : \"There was an error loading the value.\"}")
    TypedProperty<JsonObject> json;
    ...
    JsonObject jo = json.getValue();
    ```
    From a property.
    ```
    @Inject
    @ConfigJson(filePath = "/config/jsonObject.json", defaultValueProperty = "propJsonObjectDefVal")
    TypedProperty<JsonObject> json;
    ...
    JsonObject jo = json.getValue();
    ```
    From a JSON file.
    ```
    @Inject
    @ConfigJson(filePath = "/config/jsonObject.json", defaultValueFile = "/config/jsonObjectDefVal.json")
    TypedProperty<JsonObject> json;
    ...
    JsonObject jo = json.getValue();
    ```
    From a URL.
    ```
    @Inject
    @ConfigJson(filePath = "/config/jsonObject.json", defaultValueUrl = "http://host:port/resources/jsonDefVal")
    TypedProperty<JsonObject> json;
    ...
    JsonObject jo = json.getValue();
    ```
    From a class member.
    ```
    @Inject
    @ConfigJson(filePath = "/config/jsonObject.json", defaultValueIsClassMember = "methodOrFieldName")
    TypedProperty<JsonObject> json;
    ...
    JsonObject jo = json.getValue();
    ```
 
  A special case is getting the preloaded class member JSON from the `@ConfguratorSetup` annotation.  
    If we have:
    ```
    package test;
    @ConfiguratorSetup(jsonMembers = "getJson")
    public class ConfigSetup {
        public JsonObject getJson() { ... }
    }
    ```
    We can inject this JsonObject:
    ```
    @ConfigJson(property = 'test.ConfigSetup.getJson').
    ```



#### <a name="config-gotchas"></a>GOTCHAS

 - URLs for the `@ConfguratorSetup` annotation - URL paths must use a different application.  
   The CDI runtime tries to download and verify values at the deployment time, it can not be done because
   at this point the current application is not ready to serve responses to requests.  
   - Use the `@ConfgJson` annotation with the `allowDeploymentWithExceptions = false` attribute on  `@ConfguratorSetup` annotation instead.
     ```
     @Inject
     @ConfigJson(url = "http://host:port/resources/json")
     TypedProperty<JsonObject> json;
     ```
   If it is used in a CDI bean you will receive a log error during the deployment time but at runtime it will work.
   
 - The `allowDeploymentWithExceptions = false` attribute works only for CDI beans.  
   Watch out for nulls when using other APIs like JAX-RS or Servlets - it is not tested there
   and if it throws an exception the null value is injected.

 - For cases where there is only one instance per an application produced, for example Servlets
   or application scoped CDI beans, `runtime check = true` or `runtimeCheck = RuntimeCheckType.YES` will not work a typical way.  
   Use the `Instance` type to make it to work as expected.
   ```
   @Inject
   @ConfigJson(url = "http://host:port/resources/json", runtimeCheck = RuntimeCheckType.YES)
   private Instance<TypedProperty<JsonObject>> test;
   ```

---

## Docker & Kubernetes - environmental variables and configuration files.
NGINX image is used for testing purposes.

### Docker - setting environmental variables
- CLI
```
docker run -itd --name env-vars-test -e MY_PROP=myValue -e MY_PROP_DEF_VAL=myDefaultValue nginx
```
Testing
```
docker exec -it env-vars-test printenv | grep MY
	MY_PROP=myValue
	MY_PROP_DEF_VAL=myDefaultValue
```

- Docker compose
```
version: '3.3'
services:
  ev:
    container_name: env-vars-test
    image: nginx
    environment:
      - MY_PROP=myValue
      - MY_PROP_DEF_VAL=myDefaultValue
    ports:
      - 80:80
```
Testing
```
docker-compose up -d
docker exec -it env-vars-test printenv | grep MY
	MY_PROP=myValue
	MY_PROP_DEF_VAL=myDefaultValue
```

### Docker - files on the host

- Creating a proprty file on the host
```
mkdir /config
cat <<EOT >> /config/prop.properties
myProp=myValue
myPropDefVal=myDefaultValue
EOT
```

- CLI
```
docker run -itd --name files-host-test -v /config:/config nginx
```
or
```
docker run -itd --name files-host-test --mount type=bind,source=/config,target=/config nginx
```
Testing
```
docker exec -it files-host-test ls /config
	prop.properties
docker exec -it files-host-test cat /config/prop.properties
	myProp=myValue
	myPropDefVal=myDefaultValue
```

- Docker compose
```
version: "3.3"
services:
  ev:
    container_name: files-host-test
    image: nginx
    volumes:
     - type: bind
       source: /config
       target: /config
```
Testing
```
docker exec -it files-host-test ls /config
	prop.properties
docker exec -it files-host-test cat /config/prop.properties
	myProp=myValue
	myPropDefVal=myDefaultValue
```



### Kubernetes - setting environmental variables
- CLI
```
kubectl run env-vars-test --generator=run-pod/v1 --image=nginx --env=MY_PROP=myValue --env=MY_PROP_DEF_VAL=myDefaultValue
```
Testing
```
kubectl exec -it env-vars-test printenv | grep MY
	MY_PROP=myValue
	MY_PROP_DEF_VAL=myDefaultValue
```

- Using the YAML configuration file:
```
apiVersion: v1
kind: Pod
metadata:
  name: env-vars-test
spec:
  containers:
    - name: nginx
      image: nginx
      ports:
      - containerPort: 80
      env:
       - name: MY_PROP
         value: "myValue"
       - name: MY_PROP_DEF_VAL
         value: "myDefaultValue"
```
Testing
```
kubectl exec -it env-vars-test printenv | grep MY
	MY_PROP=myValue
	MY_PROP_DEF_VAL=myDefaultValue
```


### Kubernetes - files on the host

- Creating a proprty file on the host
```
mkdir /config
cat <<EOT >> /config/prop.properties
myProp=myValue
myPropDefVal=myDefaultValue
EOT
```

- Using the YAML configuration file:
```
apiVersion: v1
kind: Pod
metadata:
  name: files-host-test
spec:
  containers:
    - name: nginx
      image: nginx
      ports:
      - containerPort: 80
      volumeMounts:
      - mountPath: /config
        name: config-volume
  volumes:
   - name: config-volume
     hostPath:
       path: /config
       type: Directory
```

Testing 
```
kubectl exec -it files-host-test ls /config
	prop.properties

kubectl exec -it files-host-test cat /config/prop.properties
	myProp=myValue
	myPropDefVal=myDefaultValue
```


### Kubernetes - ConfigMap as a volume

- Using the YAML configuration file:
```
 apiVersion: v1
 kind: Pod
 metadata:
   name: cm-volume
 spec:
   containers:
     - name: nginx
       image: nginx
       ports:
       - containerPort: 80
       volumeMounts:
       - name: config-volume
         mountPath: /config/
   volumes:
     - name: config-volume
       configMap:
           name: my-config  

 ---

 apiVersion: v1
 kind: ConfigMap
 metadata:
   name: my-config
 data:
   prop.properties: |
      myProp=myValue
      myPropDefVal=myDefaultValue
```

Testing
```
kubectl exec -it cm-volume ls /config
	prop.properties
kubectl exec -it cm-volume cat /config/prop.properties
	myProp=myValue
	myPropDefVal=myDefaultValue
```


### Kubernetes - Using Secrets as files

- Creating Secrets
```
kubectl create secret generic my-secret --from-literal=myProp=myValue --from-literal=myPropDefVal=myDefaultValue
```

- Using the YAML configuration file:
```
apiVersion: v1
kind: Pod
metadata:
  name: secrets-test
spec:
  containers:
  - name: nginx
    image: nginx
    volumeMounts:
    - name: cfg
      mountPath: "/config"
      readOnly: true
  volumes:
  - name: cfg
    secret:
      secretName: my-secret
```
Testing
```
kubectl exec -it secrets-test ls /config
	myProp  myPropDefVal
kubectl exec -it secrets-test cat /config/myProp
	myValue
```


### Kubernetes - Secrets as environmental variables

- Creating Secrets
```
kubectl create secret generic my-secret --from-literal=myProp=myValue --from-literal=myPropDefVal=myDefaultValue
```

- Using the YAML configuration file:
```
apiVersion: v1
kind: Pod
metadata:
  name: secrets-env-vars
spec:
  containers:
  - name: nginx
    image: nginx
    env:
      - name: MY_PROP
        valueFrom:
          secretKeyRef:
            name: my-secret
            key: myProp
      - name:  MY_PROP_DEF_VAL
        valueFrom:
          secretKeyRef:
            name: my-secret
            key: myPropDefVal
```

Testing
```
kubectl exec -it secrets-env-vars printenv | grep MY
	MY_PROP=myValue
	MY_PROP_DEF_VAL=myDefaultValue
```



---


