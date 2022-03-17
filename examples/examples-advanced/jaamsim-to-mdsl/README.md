# Setup

This example requires JaamSim, which is does not seem to be available as a Maven artifact. 

The following steps will create a local Maven repository for JaamSim:

1. Download the "Universal Jar" from the [JaamSim website](https://jaamsim.com/downloads.html).
1. Run the following command in this directory, replacing `PATH_TO_JAAMSIM.jar` with the location of the downloaded jar: `mvn deploy:deploy-file -DgroupId=com.jaamsim -DartifactId=jaamsim -Dversion=2021-06 -Durl=file:./lib/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=PATH_TO_JAAMSIM.jar`
1. Run `mvn clean verify` to make sure Maven can resolve the JaamSim dependency from the local repository.

# Running the JaamSim to MDSL generator

You can use Maven to convert JaamSim configuration files, typically ending with `.cfg` to MDSL (replace `PATH_TO_JAAMSIM_CONFIG` with a valid path): <!-- file URL or absolute path -->

`mvn compile exec:java -Dexec.arguments="PATH_TO_JAAMSIM_CONFIG"`

Alternatively, `JaamSimToMDSL` can also be run on the command line, it has a main method.
