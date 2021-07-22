package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.StatusReport;
import io.mdsl.apiDescription.StatusReports;
import io.mdsl.transformations.DataTypeTransformationHelpers;

public class CompleteOperationWithErrorReport implements ISemanticModification  {

	public void apply(EObject element, IModificationContext context) throws Exception {
		// this is a very basic transformation, merely demonstrating the report syntax 
		Operation operation = (Operation) element;
		StatusReports srs = ApiDescriptionFactory.eINSTANCE.createStatusReports();
		StatusReport sr = ApiDescriptionFactory.eINSTANCE.createStatusReport();
		sr.setName("errorReport");
		AtomicParameter ap = DataTypeTransformationHelpers.createAtomicDataParameter("statusCode", "int");
		ElementStructure es = DataTypeTransformationHelpers.wrapAtomicParameterAsElementStructure(ap);
		sr.setReportData(es);
		srs.getReportList().add(sr);
		operation.setReports(srs);
	}
}
