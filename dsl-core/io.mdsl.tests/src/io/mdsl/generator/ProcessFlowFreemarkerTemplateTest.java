package io.mdsl.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.junit.jupiter.api.Test;

import io.mdsl.tests.AbstractMDSLInputIntegrationTest;

public class ProcessFlowFreemarkerTemplateTest extends AbstractMDSLInputIntegrationTest {
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase1Sequence() throws IOException {
		// given 
		Resource inputModel = getTestResource("flowtest1-sequence.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("flowtest1-sequence-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		String fileContent = getGeneratedFileContent("flowtest1-sequence-out.java");
		assertEquals(getExpectedTestResult("flowtest1-sequence.java"), fileContent);
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "from(\"direct:", 11)); // two flows, 7 and 4 routes
	}
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase2And() throws IOException {
		// given 
		Resource inputModel = getTestResource("flowtest2-parallelsplitwithsynchronization.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("flowtest2-parallelsplitwithsynchronization-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest2-parallelsplitwithsynchronization-out.java");
		assertEquals(getExpectedTestResult("flowtest2-parallelsplitwithsynchronization.java"), fileContent);
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "recipientList", 2)); // two flows
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "aggregate(new JoinAggregatorStrategy()).constant(true).completionSize", 1)); // one of which synchs
	}
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase3Or() throws IOException {
		// given 
		Resource inputModel = getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("flowtest3a-exclusivechoice-implicitmerge-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest3a-exclusivechoice-implicitmerge-out.java");
		assertEquals(getExpectedTestResult("flowtest3a-exclusivechoice-implicitmerge.java"), fileContent);
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "EventEmissionCondition", 2)); // three options in 1 CBR
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, ".to(\"direct:FlowStep4\")", 2)); // one "Aggregator" with two inputs
	}
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase4All() throws IOException {
		// given 
		Resource inputModel = getTestResource("flowtest4b-allbranchingoptions.mdsl");
		// note that process view tests in FlowGenModelFromMDSLFileTest work with other file "flowtest4b-andbranching-with-overlaps.mdsl"
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("flowtest4b-allbranchingoptions-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest4b-allbranchingoptions-out.java");
		assertEquals(getExpectedTestResult("flowtest4b-allbranchingoptions.java"), fileContent); 
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, ".choice()", 2)); // one CBR
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, "AND_FlowStep3Completed_FlowStep4Completed:AggregatorIn", 3)); // two inputs to this Aggregator

	}

	// this test flow starts with a command rather than an event (and contains a loop) 
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase6ESOAD() throws IOException {
		// given 
		Resource inputModel = getTestResource("esoad-blog-post.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("esoad-blog-post-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// TODO input now has three models, check the two new ones
		
		// then
		String fileContent = getGeneratedFileContent("esoad-blog-post-out.java");
		assertEquals(getExpectedTestResult("esoad-blog-post.java"), fileContent);
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, ".choice()", 3), "3 expected."); // one CBR per flow
		assertTrue(this.assertThatKeywordAppearsInExpectedNumberOfLines(fileContent, ".when(simple(", 6), "6 expected"); // with two options
	}
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowTestCase7() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest7-branchingoptions-withnesting.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("flowtest7-branchingoptions-withnesting-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest7-branchingoptions-withnesting-out.java");
		assertEquals(getExpectedTestResult("flowtest7-branchingoptions-withnesting.java"), fileContent);
		// TODO test some flow properties (see above)
	}

	// TODO test termination events and commands (all cases covered?)
	
	@Test
	public void canGenerateApacheCamelConfigurationForFlowFlowWithCombinedStep() throws IOException {
		// given
		Resource inputModel = getTestResource("test-combinedsteps-and-alloperators.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("apache-camel-configuration.java.ftl"));
		generator.setTargetFileName("test-combinedsteps-and-alloperators-out.java");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("test-combinedsteps-and-alloperators-out.java");
		assertEquals(getExpectedTestResult("test-combinedsteps-and-alloperators.java"), fileContent);
		// test some flow properties (see above)?
	}
	
	// ** flow gen model, process views, SketchMiner output:
	
	@Test
	public void canTurnBasicFlowIntoStorViaGenModel() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest0bc-hello-flow-world.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("flowtest0bc-hello-flow-world-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest0bc-hello-flow-world-out.sketch-miner");
		assertEquals(getExpectedTestResult("flowtest0bc-hello-flow-world.sketch-miner"), fileContent);
	}
	
	@Test
	public void canTurnChoiceFlow3aIntoStorViaGenModel() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest3a-exclusivechoice-implicitmerge.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("flowtest3a-exclusivechoice-implicitmerge-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest3a-exclusivechoice-implicitmerge-out.sketch-miner");
		assertEquals(getExpectedTestResult("flowtest3a-exclusivechoice-implicitmerge.sketch-miner"), fileContent);
	}
	
	// test all right-hand side composites: command choices, event choices, command and, event and
	@Test
	public void canTurnChoiceFlow4aIntoStoryViaGenModel() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest4a-alloptionsmodel1.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("flowtest4a-alloptionsmodel1-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		// TODO BPMN verified, but genmodel and path collection dumps inspected manually only for flow 3 (dep step and) here; other tests use same model (selected flows)
		// output for SampleFlowWithCisStepOr (flow 1) and SampleFlowWithCisStepAnd (flow 2) have extra event-command pair for join; correct but might not be needed  
		String fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1-out.sketch-miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1.sketch-miner"), fileContent);
	}
	
	// note: 3b and 4b, 4c not tested here but in FlowGenModelFromMDSLFileTest 

	@Test
	public void canTurnFlowWithVariationsIntoStoryViaGenModel() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest5a-syntaxvariations-canonicalgenmodel.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("flowtest5a-syntaxvariations-canonicalgenmodel-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest5a-syntaxvariations-canonicalgenmodel-out.sketch-miner");
		// TODO flow 2 in this file has lots of loops, and/or/xor bounces between E1, E2, C1, C2; check all paths and GOTOs  
		assertEquals(getExpectedTestResult("flowtest5a-syntaxvariations-canonicalgenmodel.sketch-miner"), fileContent);
	}
	
	@Test
	public void canGenerateStepStoriesForFlowTestCase6ESOAD() throws IOException {
		// given 
		Resource inputModel = getTestResource("esoad-blog-post.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("esoad-blog-post-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("esoad-blog-post-out.sketch-miner");
		assertEquals(getExpectedTestResult("esoad-blog-post.sketch-miner"), fileContent);
	}
	
	@Test
	public void canGenerateStepStoriesForFlowTestCase7() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest7-branchingoptions-withnesting.mdsl");
		TextFileGenerator generator = new TextFileGenerator();
		generator.setFreemarkerTemplateFile(getTestInputFile("process-flow-stories.sketch-miner.ftl"));
		generator.setTargetFileName("flowtest7-branchingoptions-withnesting-out.sketch-miner");

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest7-branchingoptions-withnesting-out.sketch-miner");
		assertEquals(getExpectedTestResult("flowtest7-branchingoptions-withnesting.sketch-miner"), fileContent);
	}
	
	@Test
	public void canGenerateSketchMinerBPMNForFlowTestCase4a() throws IOException {
		// given
		Resource inputModel = getTestResource("flowtest4a-alloptionsmodel1.mdsl");
		SketchMinerGenerator generator = new SketchMinerGenerator();

		// when
		JavaIoFileSystemAccess javaIoFileSystemAccess = getFileSystemAccess();
		javaIoFileSystemAccess.setOutputPath(getGenerationDirectory().getAbsolutePath());
		generator.doGenerate(inputModel, javaIoFileSystemAccess, new GeneratorContext());

		// then
		String fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1_SampleFlowWithCisStepOr.sketch_miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1_SampleFlowWithCisStepOr-expected.sketch_miner"), fileContent);
		fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1_SampleFlowWithCisStepAnd.sketch_miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1_SampleFlowWithCisStepAnd-expected.sketch_miner"), fileContent);
		fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1_SampleFlowWithDepStepAnd.sketch_miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1_SampleFlowWithDepStepAnd-expected.sketch_miner"), fileContent);
		fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1_SampleFlowWithDepStepOr.sketch_miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1_SampleFlowWithDepStepOr-expected.sketch_miner"), fileContent);
		fileContent = getGeneratedFileContent("flowtest4a-alloptionsmodel1_SampleFlowWithAllOptions.sketch_miner");
		assertEquals(getExpectedTestResult("flowtest4a-alloptionsmodel1_SampleFlowWithAllOptions-expected.sketch_miner"), fileContent);
	}
	
	@Override
	protected String testDirectory() {
		return "/test-data/flowgenmodel-tests/";
	}
}
