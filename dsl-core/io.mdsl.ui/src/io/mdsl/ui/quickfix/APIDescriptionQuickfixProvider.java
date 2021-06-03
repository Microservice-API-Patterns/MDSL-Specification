package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SingleParameterNode;
// import io.mdsl.apiDescription.impl.ElementStructureImpl;
import io.mdsl.apiDescription.impl.SingleParameterNodeImpl;
import io.mdsl.validation.DataTypeValidator;

/**
 * Custom quickfixes.
 *
 * See https://www.eclipse.org/Xtext/documentation/310_eclipse_support.html#quick-fixes
 */
public class APIDescriptionQuickfixProvider extends DefaultQuickfixProvider {
	
	class StringTypeAddition implements ISemanticModification {
		@Override
		public void apply(EObject element, IModificationContext context) throws Exception {
			GenericParameter gp = (GenericParameter) element;
			String gpn = gp.getName();
			if(gpn==null || gpn.equals("")) {
				gpn="anonymous";
			}
			else {
				// System.out.println("Fixing, using name " + gpn);
			}
			RoleAndType newRaT = ApiDescriptionFactory.eINSTANCE.createRoleAndType();
			newRaT.setName(gpn);
			newRaT.setRole("D");
			newRaT.setBtype("string");
			AtomicParameter newAP = ApiDescriptionFactory.eINSTANCE.createAtomicParameter();
			newAP.setRat(newRaT);

			/*
			// won't work when applied inside a tree (different container container object):
			SingleParameterNode newSPN = ApiDescriptionFactory.eINSTANCE.createSingleParameterNode();
			newSPN.setAtomP(newAP);
			System.out.println("About to cast " + element.eContainer().eContainer().getClass());
			ElementStructureImpl tn = (ElementStructureImpl) element.eContainer().eContainer();
			tn.setNp(newSPN);
			 */
			
			// note that exceptions are not shown, quick fix fails silently!
			// System.out.println("About to cast" + element.eContainer().getClass());
			SingleParameterNode spn = (SingleParameterNodeImpl) element.eContainer();
			spn.setGenP(null); // needed (choice)
			spn.setAtomP(newAP);
		}
	}
	
	class TypeCompletion implements ISemanticModification {
		private String type;

		public TypeCompletion(String string) {
			type = string;
		}

		@Override
		public void apply(EObject element, IModificationContext context) throws Exception {
			RoleAndType rat = (RoleAndType) element;
			rat.setBtype(type);
		}
	}

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
	
	@Fix(DataTypeValidator.TYPE_MISSING)
	public void addDefaultType(final Issue issue, IssueResolutionAcceptor acceptor) {
		// String[] ids = issue.getData();
		// acceptor.accept(issue, "Add string type", "Add D<string>.", null, (EObject element, IModificationContext context) -> ((DataContract) element).toString());
		acceptor.accept(issue, "Replace with atomic string parameter", "Use \"anonymous\":R<string> (R in D, MD ID, L).", null, new StringTypeAddition());
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addStringType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add string as type", "Add string as type: R<string> (R in D, MD ID, L).", null, new TypeCompletion("string"));
	}
	
	@Fix(DataTypeValidator.TYPE_INCOMPLETE)
	public void addIntType(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Add int as type", "Add int as type: R<int> (R in D, MD ID, L)", null, new TypeCompletion("int"));
	}
}
 
