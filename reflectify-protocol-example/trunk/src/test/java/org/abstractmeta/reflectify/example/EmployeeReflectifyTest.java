package org.abstractmeta.reflectify.example;


import org.abstractmeta.reflectify.*;
import org.abstractmeta.reflectify.core.ReflectifyRegistryImpl;
import org.abstractmeta.reflectify.example.reflectify.ExampleReflectifyProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class EmployeeReflectifyTest {

    public void testEmployee(){
        
        ReflectifyRegistry registry = new ReflectifyRegistryImpl();
        registry.registerAll(new ExampleReflectifyProvider().get());
        Reflectify<Employee> reflectify = registry.get(Employee.class);
        
        Reflectify.Provider<Employee> provider = reflectify.getProvider();
        Employee employee = provider.get();

        Mutator<Employee, Object> nameMutator1 = reflectify.getMutator("name");
        nameMutator1.set(employee, "Name 1");

        Assert.assertEquals(employee.getName(), "Name 1");

        Mutator<Employee, String> nameMutator2 = reflectify.getMutator(String.class, "name");
        nameMutator2.set(employee, "Name 2");

        Assert.assertEquals(employee.getName(), "Name 2");


        employee.setSalary(123f);

        Accessor<Employee, Object> salaryAccessor1 = reflectify.getAccessor("salary");
        float salary1 = (Float)salaryAccessor1.get(employee);

        Assert.assertEquals(salary1, 123f);



        employee.setSalary(127f);

        Accessor<Employee, Float> salaryAccessor2 = reflectify.getAccessor(Float.class, "salary");
        float salary2 = salaryAccessor2.get(employee);

        Assert.assertEquals(salary2, 127f);

        MethodInvoker<Employee, Void> setNameInvoker = reflectify.getMethodInvoker(void.class, "setName", String.class);
        setNameInvoker.getParameterSetter(0).set("abc");
        setNameInvoker.invoke(employee);

        Assert.assertEquals(employee.getName(), "abc");



    }

}
