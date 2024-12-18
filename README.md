[![pipeline status](https://git.se.informatik.uni-kiel.de/ExplorViz/code/adapter-service/badges/main/pipeline.svg)](https://git.se.informatik.uni-kiel.de/ExplorViz/code/adapter-service/-/commits/main)

[![coverage report](https://git.se.informatik.uni-kiel.de/ExplorViz/code/adapter-service/badges/main/coverage.svg)](https://git.se.informatik.uni-kiel.de/ExplorViz/code/adapter-service/-/commits/main)

# ExplorViz Adapter-Service

The entry point to ExplorViz that validates and preprocesses all incoming raw spans.

## Features

At its core, the Adapter-Service is a Kafka Streams application that steadily processes every span
emitted by a monitored application.
All monitoring data of a instrumented application is sent by a monitoring agent (like InspectIT
Ocelot Agent) via gRPC to the OpenTelemetry Collector, which in turn forwards all spans to this
service via Kafka.
In essence, the Adapter-Service performs input validation on each span.

**Input Validation**:
If any non-optional attribute is missing or invalid, the attribute is added or the span is
discarded.
This step is essential as all downstream services assume that incoming data is syntactically valid.
They are subsequently procesed by the ExplorViz/span-service.

## Prerequisites

- Java 21 (at least Java 11)
- Make sure to run
  the [ExplorViz software stack](https://git.se.informatik.uni-kiel.de/ExplorViz/code/deployment)
  before starting the service, as it provides the required database(s) and the Kafka broker

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

This also enables the `dev` configuration profile, i.e. using the properties prefixed with `%dev`
from
`src/main/resources/application.properties`.

## Code Style and Formatting

- Check code style: `./gradlew detekt`
- Check code formatting: `./gradlew spotlessCheck`
- Apply code formatting: `./gradlew spotlessApply`

## Packaging and running the application

The application can be packaged and tested using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.

You can skip running the integration tests by adding `-x integrationTest`. To skip all tests and
code analysis use the `assemble` task
instead of `build`.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.
Be aware that it’s not an _über-jar_ as the dependencies are copied into
the `build/quarkus-app/lib/` directory.

If you want to build an _über-jar_, which includes the entire application in a single jar file,
execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using
`java -jar build/adapter-service-1.0-runner.jar`.
You can add `-Dquarkus.profile=dev` to enable the `%dev` properties.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container
using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/adapter-service-1.0-runner`

If you want to learn more about building native executables, please consult
https://quarkus.io/guides/gradle-tooling#building-a-native-executable.
