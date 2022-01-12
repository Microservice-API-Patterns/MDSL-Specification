package io.mdsl.ui.handler.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.DataTypeTransformationHelpers;
import io.mdsl.transformations.DataTypeTransformations;
import io.mdsl.transformations.OperationTransformations;
import io.mdsl.ui.quickfix.dialogs.NamePromptDialog;

import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;


public class MoveOperationRefactoring extends AbstractMDSLGenerator {
	
	// support both "Move Operation" and "Extract Endpoint" from IRC, depending on input

	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		Operation moveSubject;
		try {
			EObject selectedObject = getSelectedElement();
			// check that selected object is an operation, cast, get its name  
			if(selectedObject!=null && selectedObject instanceof Operation)
				moveSubject = (io.mdsl.apiDescription.Operation) selectedObject;
			else
				throw new MDSLException("Can't refactor: no operation selected, but an instance of " + selectedObject.getClass().getSimpleName());

			// offer UI to select target endpoint name 
			String targetEndpointNme = "EndpointExtractedFor_" + moveSubject.getName();
			Shell shell = Display.getCurrent().getActiveShell();
			NamePromptDialog dialog = new NamePromptDialog(shell, "Endpoint type name", "Please enter the name of a new or an existing contract.", false);
			dialog.create();
			if (dialog.open() == Window.OK) {
				if(dialog.getFirstName()==null || dialog.getFirstName().equals(""))
					System.err.println("[E] No valid name for target endpoint type received from user interface dialog.");
				else
					targetEndpointNme = DataTypeTransformationHelpers.replaceSpacesWithUnderscores(dialog.getFirstName());
			 } else {
				// user aborted
				System.err.println("[W] No name for data type received from user interface dialog.");
			}
			
			OperationTransformations mot = new OperationTransformations();
			MDSLResource targetSpec = mot.moveOperation(moveSubject, targetEndpointNme); 

			targetSpec.save(null);
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new MDSLException("Cannot move operation as desired. " + e.getMessage());
		}
	}
	 
	@Override
	protected void generateFromServiceSpecification(ServiceSpecification mdslSpecification, IFileSystemAccess2 fsa,
			URI inputFileURI) {
		// method must be present, but is not used (unlike in Context Mapper)
	}

	// ** utilities, from Apache2 CM project (adjusted):
	
	protected IResource getCurrentResource() {
		XtextEditor xEditor = EditorUtils.getActiveXtextEditor();
		IResource xResource = xEditor.getResource();
		return xResource;
	}

	protected EObject getSelectedElement() {
		return getFirstSelectedElement(EditorUtils.getActiveXtextEditor());
	}

	private EObject getFirstSelectedElement(XtextEditor editor) {
		if (editor == null)
			return null;

		EObjectAtOffsetHelper eObjectAtOffsetHelper = new EObjectAtOffsetHelper();

		final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		EObject selectedObject = editor.getDocument().priorityReadOnly(new IUnitOfWork<EObject, XtextResource>() {
			@Override
			public EObject exec(XtextResource resource) throws Exception {
				EObject selectedElement = eObjectAtOffsetHelper.resolveElementAt(resource, selection.getOffset());
				if (selectedElement != null) {
					return selectedElement;
				}
				return null;
			}
		});
		return selectedObject;
	}
}
