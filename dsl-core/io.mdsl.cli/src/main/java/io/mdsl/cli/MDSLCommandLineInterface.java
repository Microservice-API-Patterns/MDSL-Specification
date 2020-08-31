/*
 * Copyright 2020 The MDSL Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.cli;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;

import io.mdsl.MDSLResource;
import io.mdsl.generator.TextFileGenerator;
import io.mdsl.standalone.MDSLStandaloneAPI;
import io.mdsl.standalone.MDSLStandaloneSetup;

/**
 * Simple command line tool to validate MDSL files and call generators.
 * 
 */
public class MDSLCommandLineInterface {

	private MDSLStandaloneAPI api;
	private String outputDir = "./";

	public static void main(String[] args) {
		new MDSLCommandLineInterface().run(args);
	}

	private void run(String[] args) {
		Options options = createOptions();

		CommandLineParser commandLineParser = new DefaultParser();
		try {
			CommandLine cmd = commandLineParser.parse(options, args);

			if (cmd.hasOption("help"))
				printHelp(options);

			// validate input path
			String inputPath = cmd.getOptionValue("input");
			validateInputFile(inputPath);

			// load MDSL resource
			this.api = MDSLStandaloneSetup.getStandaloneAPI();
			MDSLResource mdsl = readMDSLFile(inputPath);

			// generate output
			setOutputDir(cmd.getOptionValue("outputDir"));
			generate(mdsl, cmd.getOptionValue("generator"), cmd);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelp(options);
		}
	}

	private void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("mdsl", options);
		System.exit(1);
	}

	private Options createOptions() {
		Options options = new Options();

		// input MDSL file
		Option input = new Option("i", "input", true, "Path to the MDSL file for which you want to generate output.");
		input.setRequired(true);
		options.addOption(input);

		// generator option
		Option generator = new Option("g", "generator", true,
				"The generator you want to call. Use one of the following values: " + String.join(", ", Arrays
						.asList(MDSLGenerator.values()).stream().map(g -> g.toString()).collect(Collectors.toList())));
		options.addOption(generator);

		// output directory
		Option outputDirectory = new Option("o", "outputDir", true,
				"The output directory into which the generated files shall be written. By default files are generated into the execution directory.");
		options.addOption(outputDirectory);

		// freemarker template option
		Option freemarkerTemplate = new Option("t", "template", true,
				"Path to the Freemarker template you want to use. This parameter is only used if you pass 'text' to the 'generator' (-g) parameter.");
		options.addOption(freemarkerTemplate);

		// output file name (Freemarker generator only)
		Option outputFilename = new Option("f", "outputFile", true,
				"The name of the file that shall be generated (only used by Freemarker generator, as we cannot know the file extension).");
		options.addOption(outputFilename);

		Option help = new Option("h", "help", false, "Prints this message.");
		options.addOption(help);

		return options;
	}

	private void validateInputFile(String inputPath) {
		File inputFile = new File(inputPath);
		if (!inputFile.exists()) {
			System.out.println("ERROR: The file '" + inputPath + "' does not exist.");
			System.exit(1);
		}
		if (!inputPath.endsWith(".mdsl")) {
			System.out.println("ERROR: Please provide a path to an MDSL (*.mdsl) file.");
			System.exit(1);
		}
	}

	private void setOutputDir(String outputDir) {
		if (outputDir == null || "".equals(outputDir))
			return;

		File dir = new File(outputDir);
		if (dir.exists() && !dir.isDirectory()) {
			System.out.println("ERROR: '" + outputDir + "' is not a directory.");
			System.exit(1);
		}
		this.outputDir = outputDir;
	}

	private MDSLResource readMDSLFile(String filePath) {
		MDSLResource mdsl = api.loadMDSL(filePath);

		if (mdsl.getErrors().isEmpty()) {
			System.out.println("The MDSL file '" + filePath + "' has been compiled without errors.");
		} else {
			for (Diagnostic diagnostic : mdsl.getErrors()) {
				System.out.println("ERROR in " + diagnostic.getLocation() + " on line " + diagnostic.getLine() + ":"
						+ diagnostic.getMessage());
			}
			System.exit(1);
		}

		for (Diagnostic diagnostic : mdsl.getWarnings()) {
			System.out.println("WARNING in " + diagnostic.getLocation() + " on line " + diagnostic.getLine() + ":"
					+ diagnostic.getMessage());
		}
		return mdsl;
	}

	private void generate(MDSLResource resource, String generatorName, CommandLine cmd) {
		if (generatorName == null) {
			System.out.println("Use -g to pass the generator you want to call.");
			System.exit(1);
		}
		MDSLGenerator generator = MDSLGenerator.byName(generatorName);

		if (generator == MDSLGenerator.ARBITRATY_TEXT_BY_TEMPLATE) {
			ensureTemplatePathIsSet(cmd.getOptionValue("template"));
			ensureFileNameIsSet(cmd.getOptionValue("outputFile"));
			TextFileGenerator freemarkerGen = (TextFileGenerator) generator.getGenerator();
			freemarkerGen.setFreemarkerTemplateFile(new File(cmd.getOptionValue("template")));
			freemarkerGen.setTargetFileName(cmd.getOptionValue("outputFile"));
			api.callGenerator(resource, freemarkerGen, outputDir);
		} else {
			api.callGenerator(resource, generator.getGenerator(), outputDir);
		}
		System.out.println("The output files have been generated into '" + this.outputDir + "'.");
	}

	private void ensureTemplatePathIsSet(String pathToTemplate) {
		if (pathToTemplate == null) {
			System.out.println("ERROR: Please set the path to the Freemarker template (-t).");
			System.exit(1);
		}
		if (!pathToTemplate.endsWith(".ftl")) {
			System.out.println("ERROR: Please provide a Freemarker template file ending with *.ftl (-t).");
			System.exit(1);
		}
		File ftlFile = new File(pathToTemplate);
		if (!ftlFile.exists()) {
			System.out.println("ERROR: The Freemarker template file '" + pathToTemplate + "' does not exist.");
			System.exit(1);
		}
	}

	private void ensureFileNameIsSet(String filename) {
		if (filename == null || "".equals(filename)) {
			System.out.println(
					"Please provide a file name (-f) for the file that shall be generated. In case you use the Freemarker generator, we cannot know the appropriate file extension.");
			System.exit(1);
		}
	}

}
