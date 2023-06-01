LogUnit
=======

[![Build Status](https://github.com/netmikey/logunit/actions/workflows/build.yaml/badge.svg)](https://github.com/netmikey/logunit/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.netmikey.logunit/logunit-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.netmikey.logunit/logunit-core)

A Java library for unit-testing logging.


## Purpose

Sometimes, writing specific information into its log(file) is an important part of an application's functionality. As such, it's probably a good idea to cover that behavior in the application's unit tests.

Although there are other solutions to achieve this (like e.g. using mocks and test for the methods to be called), LogUnit aims at testing on another level: right within the logging framework, so you can still see all your logs while testing. No matter how you generate your log messages in your project, if they end up in one of the supported logging frameworks, you can use LogUnit.


## Requirements

- Java 8 or above
- JUnit 5
- You must be using one of the supported logging frameworks at test runtime
- You may use [Slf4j](https://www.slf4j.org) or your logging framework's native API (or anything else that makes your log events end up in your logging framework at runtime)

### Supported logging frameworks

- java.util.logging (the JDK's logging API)
- [Logback](https://logback.qos.ch)
- [Log4j2](https://logging.apache.org/log4j/2.x/)


## Limitations

Because of the very nature of how logging frameworks work, LogUnit cannot be used with parallel test execution. See [this issue](https://github.com/netmikey/logunit/issues/1) for a more detailed explanation.


## Installation

Add LogUnit to your project's dependencies.

* Declare `logunit-core` as compile-time dependency
* Declare the binding-specific module (e.g. `logunit-logback`, `logunit-log4j2` or `logunit-jul`) as test-runtime dependency

```
dependencies {
    ...
    testImplementation("io.github.netmikey.logunit:logunit-core:2.0.0")

    // Choose one (and only one) of the following:

    // for Logback:
    // testRuntimeOnly("io.github.netmikey.logunit:logunit-logback:2.0.0")

    // for Log4j2:
    // testRuntimeOnly("io.github.netmikey.logunit:logunit-log4j2:2.0.0")

    // for JUL:
    // testRuntimeOnly("io.github.netmikey.logunit:logunit-jul:2.0.0")
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

LogUnit wants to remain as transparent and easy to setup as possible. That means your unit tests' logs should stay the way they are (or at least as close as possible). As such, we don't want to bring and force our own Slf4j binding implementation onto consuming projects.

Therefor, LogUnit's architecture is similar to Slf4j's: At it's core, it uses the Slf4j API but in order to work at runtime, it provides binding-specific modules for hooking into the most popular logging frameworks.

