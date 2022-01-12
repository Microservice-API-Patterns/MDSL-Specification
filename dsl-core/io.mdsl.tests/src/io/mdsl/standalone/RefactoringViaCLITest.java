package io.mdsl.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.xtext.resource.SaveOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.Action;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Event;
import io.mdsl.apiDescription.EventTypes;
import io.mdsl.apiDescription.EventType;
import io.mdsl.apiDescription.IntegrationScenario;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.refactorings.AddEndpointForScenarioRefactoring;
import io.mdsl.generator.refactorings.AddEventManagementRefactoring;
import io.mdsl.generator.refactorings.AddHttpBindingRefactoring;
import io.mdsl.generator.refactorings.AddHttpResourceDuringBindingSplitRefactoring;
import io.mdsl.generator.refactorings.AddHttpResourceForURITemplateRefactoring;
import io.mdsl.generator.refactorings.AddKeyValueMapWrapperRefactoring;
import io.mdsl.generator.refactorings.AddMAPRoleRefactoring;
import io.mdsl.generator.refactorings.AddOperationsForRoleRefactoring;
import io.mdsl.generator.refactorings.AddPaginationRefactoring;
import io.mdsl.generator.refactorings.AddParameterTreeWrapperRefactoring;
import io.mdsl.generator.refactorings.AddRequestBundleRefactoring;
import io.mdsl.generator.refactorings.AddURITemplateToExistingHttpResourceRefactoring;
import io.mdsl.generator.refactorings.AddWishListRefactoring;
import io.mdsl.generator.refactorings.CompleteDataTypesRefactoring;
import io.mdsl.generator.refactorings.ExternalizeContextRepresentationRefactoring;
import io.mdsl.generator.refactorings.ExtractInformationHolderRefactoring;
import io.mdsl.generator.refactorings.InlineInformationHolderRefactoring;
import io.mdsl.generator.refactorings.MakeRequestConditionalRefactoring;
import io.mdsl.generator.refactorings.MoveOperationRefactoring;
import io.mdsl.generator.refactorings.SeparateCommandsFromQueriesRefactoring;
import io.mdsl.generator.refactorings.SplitOperationRefactoring;
import io.mdsl.generator.refactorings.TransformationChainAllInOneRefactoring;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.TransformationHelpers;

// specification of most refactorings: https://interface-refactoring.github.io/ 

public class RefactoringViaCLITest {	
	private static final String TEST_PROCESSING_RESOURCE_ENDPOINT = "TestProcessingResourceEndpoint";
	private static final String TEST_ENDPOINT = "TestEndpoint";
	private static final String TEST_OPERATION = "testOperation";

	@Test
	public void canTransformStoryToEndpointInMDSLModelViaFile() throws IOException {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMDSLResourceWithScenarioStory(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddEndpointForScenarioRefactoring("TestScenario", "TestStory"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());

		// then
		assertEquals("API description TestMDSLSpec",
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(0)); 
		// note: line 1 is empty (due to Formatter)
		assertEquals("endpoint type TestScenarioRealizationEndpoint supports scenario TestScenario",
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(2)); 
		assertEquals("operation doSomething expecting payload {\"in\":D} delivering payload {\"data\":D<string>}",
				FileUtils.readLines(file, Charset.forName("UTF-8")).get(4)); 
	}
		
	@Test
	public void canApplyMAPRoleRefactoringToMDSLModelViaFile() throws IOException {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = api.createMDSL(file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName(TEST_ENDPOINT);
		spec.getContracts().add(endpoint);
		
		// when
		api.callGenerator(newMDSLModel, new AddMAPRoleRefactoring(TEST_ENDPOINT, "PROCESSING_RESOURCE"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());

		// then
		assertEquals("API description TestMDSLSpec",
			// FileUtils.readFileToString(file, Charset.forName("UTF-8")));
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(0)); 
		// note: line 1 is empty (due to Formatter)
		assertEquals("endpoint type TestEndpoint serves as PROCESSING_RESOURCE",
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(2)); 
	}
	 
	@Test
	public void canAddOperationsCommonForMAPRoleProcessingResource() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertEquals("API description TestMDSLSpec", linesinMDSLFile.get(0)); 
		assertEquals("data type TestProcessingResourceEndpointDTO \"testProcessingResourceEndpoint\":D", linesinMDSLFile.get(1)); 
		// note: line 2 is empty (due to Formatter)
		assertEquals("endpoint type TestProcessingResourceEndpoint serves as PROCESSING_RESOURCE",
			linesinMDSLFile.get(3)); 
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "endpoint type", 1));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4));
	}
	
	/*
	@Test
	public void canExtractAndInlineInformationHolders() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMDSLResourceWithScenarioStory(api, file); 
		api.callGenerator(newMDSLModel, new AddEndpointForScenarioRefactoring("TestScenario", "TestStory"));
		// TODO set <<Embedded_Entity>> stereotype (see other tests)
		
		// when
		api.callGenerator(newMDSLModel, new ExtractInformationHolderRefactoring("TestScenarioRealizationEndpoint", "testStory")); // TODO find suited input
		api.callGenerator(newMDSLModel, new InlineInformationHolderRefactoring("TestScenarioRealizationEndpoint", "testStory")); // TODO find suited input
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertEquals("API description TestMDSLSpec", linesinMDSLFile.get(0));
		// TODO check that DTO and IHR endpoint type are (still) there, check that rep. elem. has stereotype and type reference
	}
	*/
	
	// other MAP role decorators: IHR, COLLECTION_RESOURCE (secondary role) not tested here but in MDSL2QuickFixTransformationsTest

	@Test
	public void canSplitOperation() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);

		// when
		MDSLException thrown = Assertions.assertThrows(MDSLException.class, () -> {
			api.callGenerator(newMDSLModel, new SplitOperationRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "initializeResource"));
			newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		 });
		
		// then
		Assertions.assertEquals("Split operation can only operate on operations with a Parameter Tree as request payload.", thrown.getMessage());
	}
	
	@Test
	public void canInlineInformationHolder() throws IOException {		
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		// could improve test data setup and check it
		
		// when
		MDSLException thrown = Assertions.assertThrows(MDSLException.class, () -> {
			api.callGenerator(newMDSLModel, new ExtractInformationHolderRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "initializeResource"));
			newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		 });
		
		// then
		Assertions.assertEquals("Can't find any <<Embedded_Entity>> decorator in initializeResource", thrown.getMessage());
	}
	
	@Test
	public void canExtractInformationHolder() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);

		// when
		MDSLException thrown = Assertions.assertThrows(MDSLException.class, () -> {
			api.callGenerator(newMDSLModel, new InlineInformationHolderRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "initializeResource"));
			newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		 });
		
		// then
		Assertions.assertEquals("This refactoring does not support top-level atomic parameters, but parameter trees", thrown.getMessage());
	}
	
	@Test
	public void canSeparateCommandsFromQueries() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);

		// when
		api.callGenerator(newMDSLModel, new SeparateCommandsFromQueriesRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "endpoint type", 2));
		assertEquals("endpoint type TestProcessingResourceEndpointCommands serves as PROCESSING_RESOURCE",
				linesinMDSLFile.get(3)); 
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4));
	}
		
	@Test
	public void canAddHTTPBinding() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);

		// when
		api.callGenerator(newMDSLModel, new AddHttpBindingRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "API provider", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "HTTP binding", 1));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 8)); // four operations, each must be bound
		// look for line with resource binding, assert URI
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, " at ", 2)); // 1 "at" for provider, 1 for each resource
		// not checking HTTP verbs, done in MDSL2QuickFixTransformationsTest
	}
	
	@Test
	public void canAddURITemplateToHTTPResource() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		api.callGenerator(newMDSLModel, new AddHttpBindingRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		
		// when
		api.callGenerator(newMDSLModel, new AddURITemplateToExistingHttpResourceRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "updateResourceState", "{rid}"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "API provider", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "HTTP binding", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "/{rid}", 1));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 8)); // four operations, each must be bound
	}
	
	@Test
	public void canAddHttpResourceForURITemplate() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		api.callGenerator(newMDSLModel, new AddHttpBindingRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		
		// when
		api.callGenerator(newMDSLModel, new AddHttpResourceForURITemplateRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "updateResourceState", "{rid}"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "API provider", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "HTTP binding", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, " at ", 3)); // "resource" not unique enough. 1 "at" for provider, 1 for each resource
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "/{rid}", 1));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 8)); // four operations, each must be bound
	}
	
	@Test
	public void canAddHttpResourceDuringBindingSplit() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		api.callGenerator(newMDSLModel, new AddHttpBindingRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		
		// when
		api.callGenerator(newMDSLModel, new AddHttpResourceDuringBindingSplitRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "deleteResourceState"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "API provider", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "HTTP binding", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, " at ", 3)); // "resource" not unique enough. 1 "at" for provider, 1 for each resource
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 8)); // four operations, each must be bound
	}
	
	@Test
	public void canMoveOperationToExistingEndpoint() throws IOException {
		// very similar to ExtractEndpoint (same transformation, but different input and output)
		
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName("TargetEndpoint");
		endpoint.setPrimaryRole("ANOTHER_RESOURCE");
		spec.getContracts().add(endpoint);

		// when
		api.callGenerator(newMDSLModel, new MoveOperationRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState", "TargetEndpoint"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "endpoint type", 2));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4));
	}
	
	@Test
	public void canExtractEndpoint() throws IOException {
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new MoveOperationRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState", "ExtractedEndpoint"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "endpoint type", 2));
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4));
	}
	 
	@Test
	public void canWrapParametersAndAddPagination() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddParameterTreeWrapperRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		api.callGenerator(newMDSLModel, new AddPaginationRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState", "offsetFromOperation"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4)); // four operations must be present
		// check that getResourceState operation has expected pagination metadata in request and response (could be more precise):
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "\"offset\":MD<int>", 1)); // entire getResourceState operation is in one line
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "limit-out", 1)); // could also look for offset-out, size, self, next
	}
	
	@Test
	public void canAddWishList() throws IOException {
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddParameterTreeWrapperRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		api.callGenerator(newMDSLModel, new AddWishListRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4)); // four operations must be present
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "<<Wish_List>>", 1)); 
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "\"desiredElements\":MD<string>*", 1)); 
	}
	
	@Test
	public void canAddContextRepresentation() throws IOException {
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddParameterTreeWrapperRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		api.callGenerator(newMDSLModel, new ExternalizeContextRepresentationRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4)); // four operations must be present
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "<<Context_Representation>>", 1)); // formatting issue, blanks needed
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "SampleContextDTO", 2)); // 1 operation, 1 data type definition 
	}
	
	@Test
	public void canMakeRequestConditional() throws IOException {
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddParameterTreeWrapperRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		api.callGenerator(newMDSLModel, new MakeRequestConditionalRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 4)); // four operations must be present
		// check that getResourceState operation has expected wish list metadata and stereotype:
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "<<Request_Condition>>", 1)); 
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "\"fingerprint\":MD<string>", 1)); 
	}
	
	@Test
	public void canBundleRequestsAndResponses() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");
		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMAPDecoratedEndpointWithOperations(api, file);
		
		// when
		api.callGenerator(newMDSLModel, new AddParameterTreeWrapperRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState"));
		api.callGenerator(newMDSLModel, new AddRequestBundleRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT, "getResourceState", true, true));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		// the comment makes it to MDSL-Web but is not to saved model file:
		// assertEquals("// Interface refactoring 'BundleRequests' applied.", linesinMDSLFile.get(0));  
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "<<Request_Bundle>>", 1)); // details (to be) tested in transformation unit test
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "<<Response_Bundle>>", 1)); // details (to be) tested in transformation unit test
	}
	
	@Test
	public void canCompleteDataTypesAndWrapThemInKeyValueMap() throws IOException {
		// given
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = api.createMDSL(file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName(TEST_ENDPOINT);
		spec.getContracts().add(endpoint);
		prepareOperationWithIncompleteParameterTypes(endpoint);
		
		// when
		api.callGenerator(newMDSLModel, new CompleteDataTypesRefactoring(TEST_ENDPOINT, TEST_OPERATION, "raw", true, true));
		api.callGenerator(newMDSLModel, new AddKeyValueMapWrapperRefactoring(TEST_ENDPOINT, TEST_OPERATION));
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());

		// then
		assertEquals("API description TestMDSLSpec",
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(0)); 
		// note: line 1 is empty (due to Formatter)
		assertEquals("endpoint type TestEndpoint",
			FileUtils.readLines(file, Charset.forName("UTF-8")).get(2)); 
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "D<raw>", 1)); // in response
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "mapOfP1", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "key", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "p1", 1)); // appears twice but in same line
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "p2", 1)); // appears twice but in same line
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "mapOfP2", 1));
	}
	

	@Test
	public void canAddEventManagementOperations() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMDSLResourceWithScenarioStory(api, file); 
		api.callGenerator(newMDSLModel, new AddEndpointForScenarioRefactoring("TestScenario", "TestStory"));

		// add new "receives event" to endpoint contract (see other tests)
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		
		EventType newEventType = ApiDescriptionFactory.eINSTANCE.createEventType();
		newEventType.setName("sampleEvent");
		EventTypes newEventList = ApiDescriptionFactory.eINSTANCE.createEventTypes();
		newEventList.getEvents().add(newEventType);
		spec.getEvents().add(newEventList);
		Event newEventReference = ApiDescriptionFactory.eINSTANCE.createEvent();
		newEventReference.setType(newEventType);
		EndpointContract ec = (EndpointContract)spec.getContracts().get(0);
		ec.getEvents().add(newEventReference);
		
		// when
		api.callGenerator(newMDSLModel, new AddEventManagementRefactoring("TestScenarioRealizationEndpoint", "sampleEvent")); 
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertEquals("API description TestMDSLSpec", linesinMDSLFile.get(0));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "event type", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "receives event", 1));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "operation", 3));
	}
	
	@Test
	public void canRunTransformationChain() throws IOException {
		// given 
		MDSLStandaloneAPI api = MDSLStandaloneSetup.getStandaloneAPI();
		File file = new File("./out/test-refactored-mdsl-model.mdsl");

		ensureFileDoesNotExist(file);
		MDSLResource newMDSLModel = prepareMDSLResourceWithScenarioStory(api, file); 

		// when
		api.callGenerator(newMDSLModel, new TransformationChainAllInOneRefactoring(TEST_ENDPOINT)); // TODO find suited input
		newMDSLModel.save(SaveOptions.defaultOptions().toOptionsMap());
		
		// then
		List<String> linesinMDSLFile = FileUtils.readLines(file, Charset.forName("UTF-8"));	
		assertEquals("API description TestMDSLSpec", linesinMDSLFile.get(0));
		assertEquals("endpoint type TestScenarioRealizationEndpointCommands supports scenario TestScenario serves as PROCESSING_RESOURCE",
				linesinMDSLFile.get(4)); 
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "data type", 2));
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "endpoint type", 2)); // two endpoint types (CQRS applied)
		assertTrue(hasCorrectNumberOf(linesinMDSLFile, "HTTP binding", 2)); // two endpoint types (CQRS applied)
		assertTrue(hasCorrectNumberOfOperations(linesinMDSLFile, 10)); // five operations, four of which are explicitly bound (those in command ept)
	}
	
	// ** helpers
	
	private MDSLResource prepareMDSLResourceWithScenarioStory(MDSLStandaloneAPI api, File file) {
		MDSLResource newMDSLModel = api.createMDSL(file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		IntegrationScenario scenario = ApiDescriptionFactory.eINSTANCE.createIntegrationScenario();
		scenario.setName("TestScenario");
		IntegrationStory story = ApiDescriptionFactory.eINSTANCE.createIntegrationStory();
		story.setName("TestStory");
		Action action = ApiDescriptionFactory.eINSTANCE.createAction();
		action.setPlainAction("doSomething");
		story.setAction(action);
		scenario.getStories().add(story);
		spec.getScenarios().add(scenario);
		return newMDSLModel;
	}
	
	private MDSLResource prepareMAPDecoratedEndpointWithOperations(MDSLStandaloneAPI api, File file) {
		MDSLResource newMDSLModel = api.createMDSL(file);
		ServiceSpecification spec = newMDSLModel.getServiceSpecification();
		spec.setName("TestMDSLSpec");
		EndpointContract endpoint = ApiDescriptionFactory.eINSTANCE.createEndpointContract();
		endpoint.setName(TEST_PROCESSING_RESOURCE_ENDPOINT);
		endpoint.setPrimaryRole("PROCESSING_RESOURCE");
		spec.getContracts().add(endpoint);
		api.callGenerator(newMDSLModel, new AddOperationsForRoleRefactoring(TEST_PROCESSING_RESOURCE_ENDPOINT));
		return newMDSLModel;
	}
	
	private void prepareOperationWithIncompleteParameterTypes(EndpointContract endpoint) {
		Operation operation = TransformationHelpers.createOperationWithGenericParameters(TEST_OPERATION, "p1", "n/a", true);
		SingleParameterNode spn2 = operation.getResponseMessage().getPayload().getNp();
		spn2.setGenP(null);
		AtomicParameter ap2 = DataTypeTransformations.createAtomicDataParameter("p2", null); // was: bool
		spn2.setAtomP(ap2);
		endpoint.getOps().add(operation);
	}
	
	private boolean hasCorrectNumberOfOperations(List<String> linesinMDSLFile, int expectedNumberOfOperations) {
		return hasCorrectNumberOf(linesinMDSLFile, "operation", expectedNumberOfOperations);
	}
	
	private boolean hasCorrectNumberOf(List<String> linesinMDSLFile, String keyword, int expectedNumber) {
		int actualNumber = 0;
		for(String nextLine : linesinMDSLFile) {
			if(nextLine.contains(keyword)) {
				actualNumber++;
			}
		}
		// System.out.println("Found: " + keyword + " n times: " + actualNumber);
		return actualNumber == expectedNumber;
	}
	
	private void ensureFileDoesNotExist(File file) {
		if(file.exists())
			file.delete();
	}
}
