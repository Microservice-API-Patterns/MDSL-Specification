# MDSL Xtext Project

MDSL is a Domain-specific Language (DSL) based on [Xtext](https://www.eclipse.org/Xtext/). This folder contains our Xtext project.

## Install Latest Version
The latest version of the Eclipse plugin can be installed via the following update site URL:

https://microservice-api-patterns.github.io/MDSL-Specification/updates/

## Build Locally
The project is built with [Maven](http://maven.apache.org/). You need Maven installed on your machine to build the project from the console.
Build the project with the following command: (must be executed in this directory)

```bash
mvn clean verify
```

## IDE Setup
If you want to work on our Xtext project you can import the project into Eclipse (you have to use Eclipse for Xtext) as a Maven project.

_Hint:_ Ensure you have the Xtext framework installed in your Eclipse. We recommend to use the [Eclipse IDE for Java and DSL Developers](https://www.eclipse.org/downloads/packages/release/2020-03/r/eclipse-ide-java-and-dsl-developers).

### Tycho Configurator
Before you import the project, ensure that the **Tycho configurator** is installed. You can install it in your Eclipse with the following procedure:

 1. Open the maven preferences page: _Window -> Preferences -> Maven_
 2. Under _Discovery_ press the button _Open Catalog_ and search for _Tycho_. You should find the _Tycho Configurator_ there.
 3. Select the Configurator and press _Finish_.
 4. An eclipse installation wizard will appear. Go through the wizard to finish the installation and restart Eclipse.

### Import the Project
Import the project into your Eclipse IDE as follows:

 1. In the menu open _File -> Import ..._
 2. Search for _maven_ and start the _Existing Maven Projects_ wizard.
 3. Browse to the _dsl-core_ directory of this repository and select it.
 4. Press _Select All_ to import all projects.
 5. Press _Finish_ to import the project.
 
 ## Build Eclipse Update Site Locally
 The Maven build of the project creates an Update Site as ZIP file. You can use the ZIP file to install the MDSL plugin in any Eclipse IDE.
 
 1. Run `mvn clean verify` in the `dsl-core` directory of this repository.
 2. Find the ZIP file here: `dsl-core/io.mdsl.repository/target/io.mdsl.repository-x.x.x-SNAPSHOT.zip`
 3. Use the ZIP file to install the MDSL plugin in Eclipse:
    1. _Help -> Install New Software..._
    2. Press _Add..._ and then _Archive..._ 
    3. Select the ZIP file builded above.
    4. Press _Add_.
    5. Press _Select All_ and then _Next >_.
    6. Finish the installation wizard and restart Eclipse.

## Context Mapper 
Context Mapper can generate MDSL from bounded contexts and their aggregates. So you might want to install it too.

[This Context Mapper documentation page](https://github.com/ContextMapper/context-mapper-dsl/wiki/IDE-Setup) provides further information and instructions.
