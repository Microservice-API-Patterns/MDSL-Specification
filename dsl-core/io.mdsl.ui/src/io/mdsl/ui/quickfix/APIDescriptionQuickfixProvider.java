package io.mdsl.ui.quickfix;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

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
	
	// MDSL 5.1.2: included this Xtext example; note: not implemented as a refactoring (so can cause model to enter invalid state) 
	// TODO tbd enhance or remove
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
		acceptor.accept(issue, "Wrap atomic parameter with parameter tree", "Embedd atomic parameter in a parameter tree", null, new IntroduceParameterTreeDTO());
	}
	
	// TODO (L) support PT in PT wrapping too
		
	@Fix(DataTypeValidator.AP_FOUND)  
	public void introduceKVM(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Include atomic parameter in key-value map", "Embedd atomic parameter in a key-value map", null, new IntroduceKeyValueMap());
	}

	@Fix(DataTypeValidator.INLINED_TYPE_FOUND)
	public void replaceEmbeddedTypeWithReference(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Extract data type definition", "Replace inline type with explicit type reference", null, new ConvertInlineTypeIntoTypeReference());
	}
	
	// TODO (M) offer UI to add other MAPs
	@Fix(DataTypeValidator.DECORATION_MISSING)
	public void addClassifierPagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Classify/decorate as Pagination", "Stereotype the representation element with <<Pagination>>", null, new DecorateDataType("Pagination"));
	}

	@Fix(DataTypeValidator.DECORATION_MISSING)
	public void addClassifierWishList(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Classify/decorate as Wish List", "Stereotype the representation element with <<Wish List>>", null, new DecorateDataType("Wish_List"));
	}
	
	@Fix(DataTypeValidator.DECORATION_MISSING)
	public void addClassifierRequestBundle(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Classify/decorate as Request Bundle", "Stereotype the representation element with <<Request Bundle>>", null, new DecorateDataType("Request_Bundle"));
	}
	
	// only works when applied to inlined type:
	@Fix(DataTypeValidator.PAGINATION_DECORATED)
	public void introducePagination(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO [R] could offer cursor links (pattern variant) as separate QF (same type string)
		acceptor.accept(issue, "Introduce offset-based pagination", "Enhance request parameters and response message with required metadata", null, new AddPagination("offset"));
	}
	
	// only works when applied to inlined type:
	@Fix(DataTypeValidator.WISH_LIST_DECORATED)
	public void introduceWistList(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce wish list", "Add a set-valued metadata parameter to enumerate desired reponse elements", null, new AddWishList("list"));
	}
	
	// TODO "Add Wish Template", more difficult to support but also quite useful 
	
	@Fix(DataTypeValidator.REQUEST_BUNDLE_DECORATED)
	public void introduceRequestBundle(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce request bundle", "Turn request and response payload into sets and add metadata", null, new AddRequestBundle("bothWays"));
	}
	
	// ** endpoint/operation related

	@Fix(EventValidator.RECEIVED_EVENT_FOUND)
	public void introduceEventProcessor(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Introduce event management", "Enhance endpoint contract with event processing and retrieval operations (not yet)", null, new AddEventManagementOperationsToContract("tbd"));
	}
	
	@Fix(EndpointContractValidator.HTTP_RESOURCE_BINDING_REQUIRED)
	public void introduceHTTPBinding(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add HTTP binding", "Map operations to verbs and element to parameters explicity (in mulitple resources).", null, new AddHttpProviderAndBindingForContract("Home"));
	}
	
	@Fix(HTTPBindingValidator.RESOURCE_SHOULD_BE_SPLIT)
	public void splitHTTPBinding(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Split HTTP binding", "Add an additional resource.", null, new AddHttpResourceBinding("SplitResource"));
	}
	
	// TODO [R] DPR SSD as set of QFs, SOAD method elements: "Assign role and responsibility: ..." (mostly an alignment issue)
	
	// TODO [R] Add resource collection (on endpoint level, with binding (?), "msg struct recipe"), see terminology in REST fragment mining papers
	
	@Fix(OperationValidator.NO_ERROR_REPORT)
	public void addErrorReport(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add error/status report", "e.g. HTTP 400", null, new CompleteOperationWithErrorReport());
	}
	
	@Fix(OperationValidator.NO_SECURITY_POLICY)
	public void addSecurityPolicy(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add security policy", "e.g. HTTP BA", null, new CompleteOperationWithSecurityPolicy());
	}
	
	// ** MAP related 
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleAndOperationsWithResponsibilities(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Turn into Processing Resource", "Use 'serves as PROCESSING_RESOURCE' (add suited operation stubs)", null, new TuneAsMAPProcessingResource());
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_MISSING)
	public void addEndpointRoleAndOperationsWithResponsibilitiesIHR(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO [R] offer other endpoint roles, IHR specializations, PR variants 
		acceptor.accept(issue, "Turn into Information Holder Resource", "Use 'serves as INFORMATION_HOLDER_RESOURCE'", null, new TuneAsMAPInformationHolderResource());
	}
	
	@Fix(MAPDecoratorValidator.MAP_DECORATOR_FOUND)
	public void addOperationsWithResponsibilities(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add operations common/typical for this role stereotype (or stateless helpers)", "MAP decorator in 'serves as ... ' is used for the operation suggestions", null, new AddOperationsAccordingToMAPDecoration());
	}
	
	// TODO offer more QFs on binding level (missing operation etc.)
	
	@Fix(OperationValidator.NO_COMPENSATION)
	public void addCompensation(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add compensating operation", "business-level undo (saga support)", null, new CompleteOperationWithCompensation());
	}
	
	// ** scenario/flow related
	
	// flow to flow
	
	@Fix(FlowValidator.FLOW_CIS_STEP_FOUND)
	public void addDepStepAfterCisStep(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add a domain event production step (emitting event)", "Let the new step emit a 'CommandDone' event", null, new AddApplicationFlowStep("DEP"));
	}
	
	@Fix(FlowValidator.FLOW_DEP_STEP_FOUND)
	public void addCisStepAfterDepStep(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO could add event to "receive" section of endpoint type
		acceptor.accept(issue, "Add a command invocation step (triggered by event)", "Let event trigger a new command in new step", null, new AddApplicationFlowStep("CIS"));
	}
	
	// TODO [R] orchestration validation: are all commands bound? 
	// TODO [R] validate: is command in flow binding bound to an existing operation in endpoint? offer QF if not
	
	// flow to endpoint type
	
	@Fix(FlowValidator.FLOW_FOUND)
	public void deriveEndpointTypeFromFlow(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive endpoint type from application flow", "Operations can be added later", null, new AddEndpointTypeSupportingFlow());
	}
	
	@Fix(FlowValidator.COMMAND_FOUND)
	public void addOperatiponForCommand(final Issue issue, IssueResolutionAcceptor acceptor) {
		// TODO could also add binding information to flow
		acceptor.accept(issue, "Add an operation to endpoint type that realizes this command", "STATE_TRANSITION_OPERATION", null, new AddOperationForFlowStep("STATE_TRANSITION_OPERATION"));
	}
	
	@Fix(FlowValidator.FLOW_DEP_STEP_FOUND)
	public void deriveEventProcessorOperationFromFlowStep(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive event processor operation from flow step", "EVENT_PROCESSOR", null, new AddOperationForFlowStep("EVENT_PROCESSOR"));
	}
	
	// scenarios and stories (to flow, to endpoint type)
	
	@Fix(ScenarioValidator.SCENARIO_FOUND)
	public void deriveFlowFromScenario(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Derive application flow from scenario", "Basic flow, single step (one event, one command)", null, new AddApplicationFlowForScenario());
	}
	
	@Fix(ScenarioValidator.SCENARIO_FOUND)
	public void deriveEndpointFromScenario(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add endpoint type supporting this scenario", "Create endpoint type, add operations", null, new AddEndpointTypeForScenario(true));
	}
	
	@Fix(ScenarioValidator.STORY_FOUND)
	public void defineEndpointOperationsSupportingStoryIHR(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add operation to endpoint type supporting scenario", "Create endpoint type if not present, add operation, update MAP decorator", null, new AddOperationForScenarioStory(/*"tbd"*/));
	}
	
	// TODO [R] two QFs: event type missing, command type missing (in type section); error message from validation?
}