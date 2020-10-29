# MDSL Command Line Interface (CLI)

This project contains a simple CLI to validate MDSL files and call our generators from the command line.

## Installation
Once you built the project with Gradle (call `./gradlew clean build` in the `dsl-core` directory), the CLI is available as ZIP and TAR file in the `io.mdsl.cli/build/distributions` directory.

You can also download the CLI binaries here:

 * [mdsl-cli-4.1.0.tar](https://github.com/socadk/MDSL/releases/download/v4.1.0/mdsl-cli-4.1.0.tar)
 * [mdsl-cli-4.1.0.zip](https://github.com/socadk/MDSL/releases/download/v4.1.0/mdsl-cli-4.1.0.zip)

1. Uncompress the ZIP or TAR file into the directory you want.
2. Run the CLI by using the executable in the `bin` folder.
   * Linux/Mac users: `{dir}/bin/mdsl`
   * Windows users: `{dir}\bin\mdsl.bat`
3. Optionally: add the extracted `bin` directory to you `PATH` variable, so that you can call the `mdsl` command from everywhere.

**TODO:** Besides the local build we should provide a download of the CLI ZIP and TAR files with the next release.

## Input / Usage
By just calling `./mdsl` (or `mdsl.bat` in Windows), the CLI shows you the available parameters:

```text
usage: mdsl
 -f,--outputFile <arg>   The name of the file that shall be generated
                         (only used by Freemarker generator, as we cannot
                         know the file extension).
 -g,--generator <arg>    The generator you want to call. Use one of the
                         following values: oas (Open API Specification),
                         jolie (Jolie), text (arbitraty text file by using
                         a Freemarker template), proto (Protocol Buffers),
                         graphql (GraphQL Schemas), java (Java Modulith),
                         gen-model-json (Generator model as JSON
                         (exporter)), gen-model-yaml (Generator model as
                         YAML (exporter))
 -h,--help               Prints this message.
 -i,--input <arg>        Path to the MDSL file for which you want to
                         generate output.
 -o,--outputDir <arg>    The output directory into which the generated
                         files shall be written. By default files are
                         generated into the execution directory.
 -t,--template <arg>     Path to the Freemarker template you want to use.
                         This parameter is only used if you pass 'text' to
                         the 'generator' (-g) parameter.
```

You have to pass the parameter `-i` (`--input`) with a path to an MDSL file at least (required parameter). In this case you can just compile the MDSL file and ensure it is valid.

In case you want to generate output, you have to pass `-g` and one of the generator names:
 * oas (Open API)
 * jolie (Jolie)
 * text (any textual file by using a Freemarker template)
 * proto (Protocol Buffers)
 * graphql (GraphQL schemas)
 * java (Java Modulith)
 * gen-model-json (export generator model as JSON)
 * gen-model-yaml (export generator model as YAML)
 
The parameter `-o` is optional and allows you to specify a different output directory for the generated files. By default it generates into the execution directory.

The parameters `-t` and `-f` are used for the Freemarker generator (`-g text`) only! They allow to specify the Freemarker template and the filename that shall be used for the generated file (as we cannot know the file extension).

## Examples
The following examples show all currently supported features of the CLI.

**NOTE:** Windows users have to replace `./mdsl` in the following examples with `mdsl.bat`.

### Validate MDSL File

```bash
./mdsl -i my-model.mdsl
```

**Hint**: In case your model is valid and can be compiled, you get the following output:

```bash
The MDSL file '/home/user/source/MDSL/my-model.mdsl' has been compiled without errors.
```

In case it cannot be compiled you will get an error message (exception).

### Generate Open API Specification

```bash
./mdsl -i my-model.mdsl -g oas
```

### Generate Jolie

```bash
./mdsl -i my-model.mdsl -g jolie
```

### Generate Protocol Buffers

```bash
./mdsl -i my-model.mdsl -g proto
```

### Generate GraphQL Schema's

```bash
./mdsl -i my-model.mdsl -g graphql
```

### Generate Java Code for Moduliths

```bash
./mdsl -i my-model.mdsl -g java
```

### Generate Arbitrary Text File with Freemarker Template

```bash
./mdsl -i my-model.mdsl -g text ~/source/MDSL/examples/generator-templates/FreemarkerReportDemo.md.ftl -f my-report.md
```

### Generator Model Exporters
With the following two commands it is possible to export the generator model used in MDSL as JSON or YAML:

```bash
./mdsl -i my-model.mdsl -g gen-model-json
```

```bash
./mdsl -i my-model.mdsl -g gen-model-yaml
```
