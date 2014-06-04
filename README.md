Usage
=====

TestcaseDependencyInjector
--------------------------
Utility for injecting Mocktio Mocks into testable units.

Imagine you have a component you want to test, and that component has a dependency:
```
public class MyComponent {

    private MyDependency dependency;
    
    public void someMethod(int x) {
        ...
    }
}
```
Now, in your testcase, you can do this:
```
@Mock
private MyDependency dependency;

private MyComponent systemUnderTest;

@BeforeMethod
public void setup() {
    systemUnderTest = new MyComponent();
    MockitoAnnotations.initMocks(this);
    TestcaseDependencyInjector.autoInject(this, systemUnderTest);
}
...
public void testBehavior() {
    // actual test
}

```
What will happen here is that for all `@Mock` annotated fields, the TestcaseDependencyInjector will inject them into the `systemUnderTest` IF the name of the @Mock field is the same as the name of the dependecy in the MyComponent class.
