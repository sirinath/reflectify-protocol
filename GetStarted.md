# Introduction #

This library provides fast reflection. All operation for a given type are aggregated, and the following are available

  * Provider - responsible for creating a new instance of a given type.
  * Accessor - responsible for getting a value of a given field.
  * Mutator - responsible for setting value of a given field.
  * MethodInvoker - responsible for invoking a given method.

# Details #

Take for example the following Employee class

```

public class Employee {
    private int id;
    private String name;
    private Department department;
    private float salary;
    //constructors
    ...
    
    //getters and setters
    ...
}


```

You can use this library for reflection in the following way

### Instantiate Reflectify ###

#### Runtime generated ####

```


    ReflectifyRegistry registry = new ReflectifyRuntimeRegistry(); 
    Reflectify<Employee> reflectify = registry.get(Employee.class);

```



#### Statically generated reflectify code (maven plugin) ####

```

    // for statically generated reflectify implementation 
    ReflectifyRegistry registry = new ReflectifyRegistryImpl();
    registry.registerAll(new ExampleReflectifyProvider().get());
    Reflectify<Employee> reflectify = registry.get(Employee.class);


```


### Using Provider ###
```

    //for zero argument constructor
    Reflectify.Provider<Employee> provider = reflectify.getProvider();
    Employee employee = provider.get();
   
   //for String.class, int.class, ... constructor parameters
   Reflectify.Provider<Employee> provider = reflectify.getProvider(String.class, int.class, ...); 
    Employee employee = provider.get();
 

```

### Using Mutator ###
```

 
    Mutator<Employee, String> nameMutator = reflectify.getMutator(String.class, "name");
    nameMutator.set("Name 1");

   //or

    Mutator<Employee, Object> nameMutator = reflectify.getMutator("name");
    nameMutator.set(employee, "Name 1");

```


### Using Accessor ###
```

    Accessor<Employee, Float> salaryAccessor = reflectify.getAccessor(Float.class, "salary");
    float salary = salaryAccessor.get(employee);

    //or
    Accessor<Employee, Object> salaryAccessor = reflectify.getAccessor("salary");
    float salary = salaryAccessor.get(employee);


```

### Method invoker ###

```

        MethodInvoker<Employee, Void> setNameInvoker = reflectify.getMethodInvoker(void.class, "setName", String.class);
        
        //method parameter setting starting form index 0 for 1st parameter
        setNameInvoker.getParameterSetter(0).set("abc");
        

       setNameInvoker.invoke(employee);


```


## Maven configuration ##

This library is managed by maven central repository, at this time however it is only in the snapshot repository.

### Maven dependency ###
```
<dependency>
  <groupId>org.abstractmeta</groupId>
    <artifactId>reflectify-protocol-core</artifactId>
    <version>0.3.0</version>
</dependency>
```

#### Dynamic runtime reflectify code generation dependency ####
```
<dependency>
    <groupId>org.abstractmeta</groupId>
    <artifactId>reflectify-protocol-runtime</artifactId>
    <version>0.3.0</version>
</dependency>
```


#### Static reflectify code generation plugin configuration ####

for example plugin configuration for all 'org.abstractmeta.reflectify.example' package types
```

<plugin>
    <groupId>org.abstractmeta</groupId>
    <artifactId>code-g-maven-plugin</artifactId>
    <version>0.4.0</version>
    <dependencies>
        <dependency>
            <groupId>org.abstractmeta</groupId>
            <artifactId>reflectify-protocol-codegen</artifactId>
            <version>0.3.0</version>
  </dependency>
    </dependencies>
    <configuration>
        <units>
            <unit>
                <sourcePackage>org.abstractmeta.reflectify.example</sourcePackage>
                <descriptors>
                    <descriptor>
                        <plugin>org.abstractmeta.reflectify.plugin.ReflectifyGenerator</plugin>
                    </descriptor>
                </descriptors>
            </unit>
        </units>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## More examples ##

For more example check out the following example project
[example](https://reflectify-protocol.googlecode.com/svn/reflectify-protocol-example/trunk/)