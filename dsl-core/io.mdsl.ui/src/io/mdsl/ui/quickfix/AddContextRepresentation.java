package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Operation;
import io.mdsl.transformations.MessageTransformations;
import io.mdsl.transformations.TransformationHelpers;
import io.mdsl.utils.MDSLLogger;

class AddContextRepresentation extends QuickfixSemanticModification {
	private static final String SAMPLE_CONTEXT = "SampleContext";

	public AddContextRepresentation() {
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		if (!(element instanceof Operation)) {
			MDSLLogger.reportError("This type of Quick Fix can only be applied if an operation is selected.");
		}

		Operation operation = (Operation) element;
		if (operation.getRequestMessage() == null && operation.getRequestMessage().getPayload() == null) {
			MDSLLogger.reportError("This type of Quick Fix can only be applied if operation has a request payload.");
		}

		String typeName = ConvertInlineTypeToTypeReference.obtainTypeNameFromUser();
		if (typeName == null || typeName.equals("")) {
			typeName = SAMPLE_CONTEXT;
		}

		/*
		ServiceSpecification ss = (ServiceSpecification) element.eContainer().eContainer();
		ElementStructure es = DataTypeTransformations.wrapAtomicParameterAsElementStructure(
			DataTypeTransformations.createMetadataParameter("qos", "int"));
		DataContract contextDTO = DataTypeTransformations.findOrCreateDataType(ss, typeName, es);
		MessageTransformations.addContextRepresentation(operation.getRequestMessage().getPayload(), contextDTO);
		*/	
		MessageTransformations.addContextRepresentation(operation, typeName);
	}
}