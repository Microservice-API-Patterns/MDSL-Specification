package io.mdsl.ui.quickfix;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

import io.mdsl.transformations.FlowTransformations;
import io.mdsl.validation.DataTypeValidator;
import io.mdsl.validation.EndpointContractValidator;
import io.mdsl.validation.EventValidator;
import io.mdsl.validation.FlowValidator;
import io.mdsl.validation.HTTPBindingValidator;
import io.mdsl.validation.MAPDecoratorValidator;
import io.mdsl.validation.OperationValidator;
import io.mdsl.validation.ScenarioValidator;

/**
 * Custom quickfixes.
 *
 * See https://www.eclipse.org/Xtext/documentation/310_eclipse_support.html#quick-fixes
 */
public class APIDescriptionQuickfixProvider extends DefaultQuickfixProvider {

	// ** quick fix implementation classes, providing callbacks (that are called by the fixes):
	
	// MDSL 5.1.2: included this Xtext example; not implemented as a refactoring (so can cause model to enter invalid state) 
	@Fix(DataTypeValidator.LOWER_CASE_NAME)
		public void capitalizeName(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Capitalize name", "Use an upper case letter to start the name.", "upcase.png", new IModification() {
			public void apply(IModificationContext context) throws BadLocationException {
				IXtextDocument xtextDocument = context.getXtextDocument();
				String firstLetter = xtextDocument.get(issue.getOffset(), 1);
				xtextDocument.replace(issue.getOffset(), 1, firstLetter.toUpperCase());
			}
		});
	}
	
	// ** data type related 
	
	@Fix(DataTypeValidator.TYPE_MISSING)
	public void addDefaultType(final Issue issue, IssueResolutionAcceptor acceptor) {
		// String[] ids = issue.getData();
		// acceptor.accept(issue, "Add string type", "Add D<string>.", null, (EObject element, IModificationContext context) -> ((DataContract) element).toString());
		acceptor.accept(issue, "Replace with atomic string parameter", "Use \"anonymous\":R<string> (R in D, MD ID, L).", null, new ConvertToStringDataType());
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addStringType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add string as type", "Add string as type: Role<string> (Role is one of D, MD ID, L).", null, new CompleteDataType("string"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addIntType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add int as type", "Add int as type: Role<int> (Role is one of D, MD ID, L)", null, new CompleteDataType("int"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addBoolType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add bool as type", "Add boolean as type: Role<bool> (Role is one of D, MD ID, L)", null, new CompleteDataType("bool"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addLongType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add long as type", "Add long as type: Role<long> (Role is one of D, MD ID, L)", null, new CompleteDataType("long"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addDoubleType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add double as type", "Add double as type: Role<double> (Role is one of D, MD ID, L)", null, new CompleteDataType("double"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addRawType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add raw as type", "Add raw as type: Role<raw> (Role is one of D, MD ID, L)", null, new CompleteDataType("raw"));
	}
	
	@Fix(DataTypeValidator.APL_FOUND)
	public void replaceAPLWithPT(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Replace atomic parameter list with parameter tree", "Use parameter tree instead of atomic parameter list", null, new IntroduceParameterTreeDTO());
	}
	
	@Fix(DataTypeValidator.AP_FOUND)
	public void wrapAPWithPT(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Wrap atomic parameter with parameter tree", "Embed atomic parameter in a parameter tree", null, new IntroduceParameterTreeDTO());
	}
	
	// TODO (M) support PT in PT wrapping too, and PT in type reference
	
	@Fix(DataTypeValidator.TR_FOUND)
	public void wrapTRWithPT(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Wrap type reference in parameter tree", "Embed data type reference in a parameter tree", null, new IntroduceParameterTreeDTO()); // TODO test
	}
	
	@Fix(DataTypeValidator.AP_FOUND)  
	public void introduceKVM(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Include atomic parameter in key-value map", "Embed atomic parameter in a key-value map", null, new IntroduceKeyValueMap());
	}

	@Fix(DataTypeValidator.INLINED_TYPE_FOUND)
	public void replaceEmbeddedTypeWithReference(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Extract data type definition", "Replace inline type with explicit type reference (a.k.a. Introduce DTO)", null, new ConvertInlineTypeToTypeReference());
	}
	
	/*
	// no longer in use:
	@Fix(DataTypeValidator.DECORATION_MISSING)
	public void addClassifierPagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Classify/decorate as Pagination", "Stereotype the representation element with <<Pagination>>", null, new DecorateDataType("Pagination"));
	}

	// no longer in use:
	// only works when applied to inlined type:
	@Fix(DataTypeValidator.PAGINATION_DECORATED)
	public void introducePaginationFromStereotype(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO [R] could offer cursor links (pattern variant) as separate QF (same type string)
		acceptor.accept(issue, "Introduce offset-based pagination", "Enhance request parameters and response message with required metadata", null, new AddPagination("offsetFromStereotype"));
	}
	*/
	
	// ** endpoint/operation related

	@Fix(EventValidator.RECEIVED_EVENT_FOUND)
	public void introduceEventProcessor(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce event management", "Enhance endpoint contract with event processing and retrieval operations (not yet)", null, new AddEventManagementOperationsToContract("tbd"));
	}
	
	@Fix(OperationValidator.NO_ERROR_REPORT)
	public void addErrorReport(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add error/status report", "e.g. HTTP 400", null, new CompleteOperationWithErrorReport());
	}
	
	@Fix(OperationValidator.NO_SECURITY_POLICY)
	public void addSecurityPolicy(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add security policy", "e.g. HTTP BA", null, new CompleteOperationWithSecurityPolicy());
	}

	@Fix(OperationValidator.NO_COMPENSATION)
	public void addCompensation(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add compensating operation", "business-level undo (saga support)", null, new CompleteOperationWithCompensation());
	}
	
	@Fix(OperationValidator.OPERATION_FOUND)
	public void moveOperationOrExtractEndpoint(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Move operation/extract endpoint", "target endpoint may ot may not exist", null, new ExtractEndpointOrMoveOperation("NewEndpoint"));
	}
	
	// HTTP binding related
	
	@Fix(EndpointContractValidator.HTTP_RESOURCE_BINDING_MISSING)
	public void provideHTTPBinding(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO improve naming: UI? get from issue?
		acceptor.accept(issue, "Provide HTTP binding", "Map operations to verbs and element to parameters explicitly (in multiple resources).", null, new AddHttpProviderAndBindingForContract("Home")); 
	}
	
	@Fix(HTTPBindingValidator.RESOURCE_SHOULD_BE_SPLIT)
	public void splitHTTPBinding(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO improve naming: UI? get from issue?
		acceptor.accept(issue, "Split HTTP binding", "Add an additional resource.", null, new AddHttpResourceDuringBindingSplit("Resource")); 
	}
	
	@Fix(HTTPBindingValidator.GLOBAL_PARAMETER_BINDING_FOUND)
	public void individualizeHTTPParameterBinding(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO (L) could offer to do this for all operation in an endpoint in one go
		acceptor.accept(issue, "Bind message elements to HTTP parameters individually", "PATH, QUERY, BODY, etc.", null, new AddHttpParameterBindingsForElements()); 
	}
	
	@Fix(HTTPBindingValidator.URI_TEMPLATE_FOR_PATH_PARAM_MISSING)
	public void addParameterToResourePath(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Extend resource URI with template for PATH parameter", "See RFC nnn for {uri} syntax", null, new AddURITemplateToExistingHttpResource("{id}", issue.getMessage())); 
	}
	
	@Fix(HTTPBindingValidator.URI_TEMPLATE_FOR_PATH_PARAM_MISSING)
	public void extractHTTPResourceForTemplateURI(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Move operation binding to new resource with URI template for PATH parameter", "See RFC nnn for {uri} syntax.", null, new AddHttpResourceForURITemplate("{id}", issue.getMessage())); 
	}
	
	// ** MAP related 
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleProcessingResource(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Processing Resource", "Use 'serves as PROCESSING_RESOURCE' (add suited operation stubs)", null, new DecorateWithMAPEndpointRole("PROCESSING_RESOURCE"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleInformationHolderResource(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Information Holder Resource", "Use 'serves as INFORMATION_HOLDER_RESOURCE'", null, new DecorateWithMAPEndpointRole("INFORMATION_HOLDER_RESOURCE"));
	}
		
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleDTR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Data Transfer Resource", "Use 'serves as DATA_TRANSFER_RESOURCE'", null, new DecorateWithMAPEndpointRole("DATA_TRANSFER_RESOURCE"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleLLR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Link Lookup Resource", "Use 'serves as LINK_LOOKUP_RESOURCE'", null, new DecorateWithMAPEndpointRole("LINK_LOOKUP_RESOURCE"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleMDH(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Master Data Holder", "Use 'serves as MASTER_DATA_HOLDER'", null, new DecorateWithMAPEndpointRole("MASTER_DATA_HOLDER"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleODH(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Operational Data Holder", "Use 'serves as OPERATIONAL_DATA_HOLDER'", null, new DecorateWithMAPEndpointRole("OPERATIONAL_DATA_HOLDER"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleRDH(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate as Reference Data Holder", "Use 'serves as REFRENCE_DATA_HOLDER'", null, new DecorateWithMAPEndpointRole("REFERENCE_DATA_HOLDER"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleCR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Mark as Collection Resource", "Only useful if primary role is INFORMATION_HOLDER", null, new DecorateWithMAPEndpointRole("COLLECTION_RESOURCE"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleMCR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Mark as Mutable Collection Resource", "Only useful if primary role is INFORMATION_HOLDER", null, new DecorateWithMAPEndpointRole("MUTABLE_COLLECTION_RESOURCE"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRole(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Decorate with arbitrary role stereotype", "Any string can be supplied", null, new DecorateWithMAPEndpointRole("tbd"));
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_FOUND)
	public void addOperationsWithResponsibilities(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add operations common/typical for role stereotypes", "MAP decorators in 'serves as ... ' are used for the operation suggestions", null, new AddOperationsAccordingToMAPDecoration());
	}
	
	// ** IRC related
	
	// TODO (future work) also support AddVersionMediator
	
	@Fix(OperationValidator.REQUEST_IS_PARAMETER_TREE_WITH_OPTIONAL_NODES)
	public void splitOperation(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Split operation", "Use request parameter tree as CSV list of request data split, keep response", null, new SplitOperation(false));
	}
	
	@Fix(MAPDecoratorValidator.CQRS_ELIGIBLE)
	public void introduceCQRS(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Segregate commands from queries", "Apply CQRS pattern by moving retrieval operations to new endpoint", null, new SegregateCommandsFromQueries());
	}
	
	@Fix(OperationValidator.MAP_WISH_LIST_POSSIBLE)
	public void introduceWishList(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add Wish List", "Add a set-valued metadata parameter to enumerate desired reponse elements", null, new AddWishList("fromOperation"));
	}
	
	@Fix(OperationValidator.MAP_WISH_TEMPLATE_POSSIBLE)
	public void addWishTemplate(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add Wish Template", "Copy response tree and stereotype the representation element with <<Wish_Template>>", null, new AddWishTemplate());
	}
	
	// Add Context Representation and Add Request Bundle share precondition (request payload must be a PT)
	@Fix(OperationValidator.MAP_REQUEST_BUNDLE_POSSIBLE)
	public void introduceContextRepresentation(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Externalize Context Representation", "Add QoS metadata as request DTO and stereotype the representation element with <<Context_Representation>", null, new AddContextRepresentation());  
	}
	
	// Add Request Bundle and Make Request Conditional share precondition (request payload must be a PT)
	@Fix(OperationValidator.MAP_REQUEST_BUNDLE_POSSIBLE)
	public void makeRequestConditional(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Make request conditional", "Add fingerprint metadata and stereotype the new element with <<Request_Condition>>", null, new MakeRequestConditional("fingerprint")); // there also is last-modified  
	}
	
	// Add Context Representation and Add Request Bundle share precondition (request payload must be a PT)
	@Fix(OperationValidator.MAP_REQUEST_BUNDLE_POSSIBLE)
	public void introduceRequestBundle(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Bundle requests", "Turn request payload into set (and add metadata)", null, new AddRequestBundle("fromOperationRequest"));
	}
	
	@Fix(OperationValidator.MAP_RESPONSE_BUNDLE_POSSIBLE)
	public void introduceResponseBundleBundle(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Bundle responses", "Turn response payload into set (and add metadata)", null, new AddRequestBundle("fromOperationResponse")); // TODO name/parameter 
	}
	
	@Fix(OperationValidator.MAP_PAGINATION_POSSIBLE)
	public void introducePagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce Page-Based Pagination", "Enhance request parameters and response message with required metadata", null, new AddPagination("pageFromOperation"));
	}
	
	@Fix(OperationValidator.MAP_PAGINATION_POSSIBLE)
	public void introduceOBPagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce Offset-Based Pagination", "Enhance request parameters and response message with required metadata", null, new AddPagination("offsetFromOperation"));
	}
	
	@Fix(OperationValidator.MAP_PAGINATION_POSSIBLE)
	public void introduceCBPagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce Cursor-Based Pagination", "Enhance request parameters and response message with required metadata", null, new AddPagination("cursorFromOperation"));
	}
		
	@Fix(OperationValidator.EMBEDDED_ENTITY_FOUND_IN_REQUEST)
	public void extractIHRFromRequestMessage(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Extract Information Holder (from request parameter tree)", "New endpoint with lookup operation", null, new ExtractInformationHolder("fromRequest"));
	}
		
	@Fix(OperationValidator.EMBEDDED_ENTITY_FOUND)
	public void extractIHRFromResponseMessage(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Extract Information Holder (from response parameter tree)", "New endpoint with lookup operation", null, new ExtractInformationHolder("fromResponse")); // TODO name/parameter
	}
	
	@Fix(OperationValidator.LINKED_INFORMATION_HOLDER_FOUND_IN_REQUEST)
	public void inlineIHRInRequestMessage(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Inline Information Holder (from request parameter tree)", "Replace link with type reference", null, new InlineInformationHolder("fromRequest"));
	}
	
	@Fix(OperationValidator.LINKED_INFORMATION_HOLDER_FOUND)
	public void inlineIHRInResponseMessage(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Inline Information Holder (from response parameter tree)", "Replace link with type reference", null, new InlineInformationHolder("fromResponse"));
	}
	
	// ** scenario/flow related
	
	// TODO (future work) two QFs: event type missing, command type missing (in type sections); error message from validation?
	// TODO (future work) offer QF to generate flow binding (which is experimental still)
	// TODO (future work) orchestration validation: all commands in flow binding bound to an existing operation in endpoint? offer QF if not
	
	// * flow to flow
	
	@Fix(FlowValidator.FLOW_CIS_STEP_FOUND)
	public void addDepStepForCommand(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add domain event production step(s) for command(s)", "Let command(s) in this step emit 'CommandDone' events)", null, new AddApplicationFlowStep(FlowTransformations.DEP_STEP));
	}
	
	@Fix(FlowValidator.FLOW_DEP_STEP_FOUND)
	public void addCisStepForEvent(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add command invocation step(s) triggered by event", "Let event(s) of this step trigger a new command", null, new AddApplicationFlowStep(FlowTransformations.CIS_STEP));
	}
	
	@Fix(FlowValidator.FLOW_DEP_STEP_FOUND) 
	public void injectParallelBranch(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Inject a parallel branch (with join)", "Two events, two commands, one aggregator", null, new AddBranchWithMerge("AND"));
	}
	
	@Fix(FlowValidator.FLOW_CIS_STEP_FOUND)
	public void injectAlternativeBranch(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Inject a choice branch (with join)", "Two events, two commands, one aggregator", null, new AddBranchWithMerge("OR"));
	}
	
	@Fix(FlowValidator.FLOW_ECE_STEP_FOUND)
	public void splitCombinedStep(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Split step into a command invocation and an event production", "Semantically identical", null, new CombinedFlowStepSplit());
	}
	
	@Fix(FlowValidator.FLOW_SIMPLE_DEP_STEP_FOUND) 
	public void mergeCommandStepWithPeers(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Merge all single event productions of this command", "Replaces simple flow steps with inclusive 'or'", null, new ConsolidateFlowSteps("OR"));
	}
	
	// * flow to BPMN
	
	@Fix(FlowValidator.FLOW_FOUND)
	public void generateSketchMinerStory(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Display as BPMN via SketchMiner", "tbc", null, new ConvertFlowToBPMN());
	}
	
	// * flow to endpoint type
	
	@Fix(FlowValidator.FLOW_WITHOUT_SUPPORTING_ENDPOINT_TYPE_FOUND)
	public void deriveEndpointTypeFromFlow(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive endpoint type from application flow", "Operations can be added later", null, new AddEndpointTypeSupportingFlow());
	}
	
	@Fix(FlowValidator.FLOW_CIS_STEP_FOUND) // @Fix(FlowValidator.COMMAND_FOUND)
	public void addOperationForCommand(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO could also add binding information to flow
		acceptor.accept(issue, "Derive operation(s) that realize(s) command(s)", "and add them to endpoint realizing the flow", null, new AddOperationForFlowStep(FlowTransformations.CIS_STEP));
	}
	
	@Fix(FlowValidator.FLOW_DEP_STEP_FOUND) // @Fix(FlowValidator.EVENT_FOUND)
	public void deriveEventProcessorOperationFromFlowStep(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive event handler operation(s) for flow step", "and add it/them to endpoint realizing the flow", null, new AddOperationForFlowStep(FlowTransformations.DEP_STEP));
	}
	
	// * scenarios and stories (to flow, to endpoint type)
	
	@Fix(ScenarioValidator.SCENARIO_FOUND)
	public void deriveFlowFromScenario(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive application flow from scenario", "Basic flow, single step (one event, one command)", null, new AddApplicationFlowForScenario());
	}
	
	@Fix(ScenarioValidator.SCENARIO_FOUND)
	public void deriveEndpointFromScenario(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive endpoint type supporting this scenario", "Create endpoint type, add operations", null, new AddEndpointTypeForScenario(true));
	}
	
	@Fix(ScenarioValidator.STORY_FOUND)
	public void defineEndpointOperationsSupportingStoryIHR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add operation to endpoint type supporting scenario", "Create endpoint type if not present, add operation, update MAP decorator", null, new AddOperationForScenarioStory(/*"tbd"*/));
	}
	
	@Fix(ScenarioValidator.SCENARIO_FOUND)
	public void applyFullTransformationChain(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive quality-assured, pattern-oriented endpoint type", "Run transformation chain", null, new AddEndpointTypeWithPatternSupport("performance"));
	}
}