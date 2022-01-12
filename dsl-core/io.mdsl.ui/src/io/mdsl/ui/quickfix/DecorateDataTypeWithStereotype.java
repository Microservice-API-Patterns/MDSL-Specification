package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.transformations.TransformationHelpers;

// TODO (L) not in use at present:
public class DecorateDataTypeWithStereotype extends QuickfixSemanticModification {

	private String decorator; // use to distinguish between cursor-based and offset-based

	public DecorateDataTypeWithStereotype(String decorator) {
		this.decorator = decorator;
	}

	@Override
	public void performQuickfix(EObject element, IModificationContext context) {

		// TODO check value of decorator: Pagination? Wish_List? Request_Bundle?

		if (element instanceof ParameterTree) {
			ParameterTree pt = (ParameterTree) element;
			PatternStereotype classifier = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
			classifier.setPattern(decorator); // TODO other cases: eip, other
			pt.setClassifier(classifier);
		} else if (element instanceof AtomicParameter) {
			if (decorator.equals("Pagination")) {
				TransformationHelpers.reportError("Pagination decorator should only be applied to parameter trees. Please wrap the atomic paramater before decorating it this way.");
			}

			AtomicParameter ap = (AtomicParameter) element;
			PatternStereotype classifier = ApiDescriptionFactory.eINSTANCE.createPatternStereotype();
			classifier.setPattern(decorator); // TODO other cases: eip, other
			ap.setClassifier(classifier);
		}
	}
}
