# MDSL Standalone Example

This little project illustrates how MDSL can be used as a library in a Java application. It currently shows the following two use cases:

 * Read an MDSL model in Java
 * Call the generators provided by the MDSL project from your Java code
 
## Preconditions
We haven't published the MDSL library into a public Maven repository yet. If you want to run the examples in this project, you have to install the library in your local Maven repository first.
Just run the following command in the `dsl-core` folder of this repository:

```bash
./gradlew clean publishToMavenLocal
```

## Build
This example project is built with Gradle. Once you installed the MDSL library in your local Maven repository (see command above), you can build this example project with the following command in this directory:

```bash
./gradlew clean build
```

### IDE
You can import this project into your favorite IDE by using the Gradle build.

## The Examples
Under [src/main/java/io/mdsl/standalone/examples](./src/main/java/io/mdsl/standalone/examples) you can find Java classes with main methods that illustrate the following use cases:

 * [Read an MDSL model](./src/main/java/io/mdsl/standalone/examples/ReadingMDSL.java)
 * [Generate OpenAPI (OAS) specifications](./src/main/java/io/mdsl/standalone/examples/OpenAPIGeneration.java)
 * [Generate Protocol Buffers (*.proto files)](./src/main/java/io/mdsl/standalone/examples/ProtocolBuffersGeneration.java)
 * [Generate Jolie](./src/main/java/io/mdsl/standalone/examples/JolieGeneration.java)
 * [Generate arbitrary text files with Freemarker templates](./src/main/java/io/mdsl/standalone/examples/FreemarkerTextGeneration.java)

