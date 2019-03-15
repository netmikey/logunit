LogUnit
=======

A framework for unit-testing logging.


## Purpose

Sometimes, writing specific information into its log(file) is an important part of an application's functionality. As such, it's probably a good idea to cover that behavior in the application's unit tests.

Although there are other solutions to achieve this (like e.g. encapsulate important logging into dedicated Java interfaces, inject mocks of those interfaces into the unit and test for the methods to be called), LogUnit aims at testing on a  lower level, right within the logging framework. No matter how you generate your log messages in your project, if they end up in the popular [Slf4j](https://www.slf4j.org) library, you can use LogUnit (\*).


## Requirements

- Java 8 or above
- JUnit 5
- You must be using Slf4j for logging
- You must be using Logback as log binding (\*)


## Installation

Add LogUnit to your project's dependencies.

* Declare `logunit-core` as compile-time dependency
* Declare the binding-specific module (e.g. `logunit-logback` (\*) as test-runtime dependency

```
dependencies {
    ...
    testImplementation("io.github.netmikey.logunit:logunit-core:1.0.1")
    testRuntimeOnly("io.github.netmikey.logunit:logunit-logback:1.0.1")
}
```


## Usage

Let's say we have a unit to be tested that looks something like this:

``` Java
public class MyModule {
    private static final LOG = LoggerFactory.getLogger(MyModule.class);

    public void bobDoSomething() {
        // ...
        LOG.info("Bob did something.");
    }
}
```

Within `MyModule`'s test class, we register a `LogCapturer` for the logger of type `MyModule` as a JUnit extension:

``` java
public class MyModuleTest {

    @RegisterExtension
    LogCapturer logs = LogCapturer.create().captureForType(MyModule.class);

```

In our test method, we can use this extension to query the log events (messages) that have been emitted by the `MyModule` logger:

``` java
@org.junit.jupiter.api.Test
public void testBobDoSomethingLogging() {

    MyModule tested = new MyModule();
    tested.bobDoSomething();

    // Run assertions on the logged messages
    logs.assertContains("Bob did something");
}
```

By default, only the log levels `INFO` and above are being captured. You can capture multiple loggers in a single `LogCapturer` and raise or lower the threshold log level to be captured per logger like this:

``` java
    @RegisterExtension
    LogCapturer logs = LogCapturer.create()
        .captureForType(MyModule.class, Level.WARN)
        .captureForLogger("LOGGER_NAME", Level.DEBUG);
```

See [LogCapturerWithLogbackTest.java](https://github.com/netmikey/logunit/blob/master/logunit-logback/src/test/java/io/github/netmikey/logunit/logback/LogCapturerWithLogbackTest.java) for more in-depth examples.


## Architecture

LogUnit wants to remain as transparent as possible. That means your unit tests' logs should stay the way they are (or at least as close as possible). As such, we don't want to bring and force our own Slf4j binding implementation onto consuming projects.

Therefor, LogUnit's architecture is similar to Slf4j's: At it's core, it uses the Slf4j API but in order to work at runtime, it provides binding-specific modules for the most popular logging frameworks (\*).


## Limitations

(\*) Currently, only Logback is supported.
