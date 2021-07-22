package io.mdsl.ui.handler.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.exception.MDSLException;
import io.mdsl.generator.AbstractMDSLGenerator;
import io.mdsl.transformations.OperationTransformations;

import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;


public class MoveOperationRefactoring extends AbstractMDSLGenerator {
 
	// Set<EObject> objects = getAllSelectedElements();
	// ar.refactor(resource, getAllResources());
	// ar.persistChanges(serializer);

	@Override
	public void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		Operation moveSubject;
		try {
			EObject selectedObject = getSelectedElement();
			// check that selected object is an operation, cast, get its name  
			if(selectedObject!=null && selectedObject.getClass() == io.mdsl.apiDescription.impl.OperationImpl.class)
				moveSubject = (io.mdsl.apiDescription.Operation) selectedObject;
			else
				throw new MDSLException("Can't refactor: no operation selected, but " + selectedObject.getClass().toString());

			OperationTransformations mot = new OperationTransformations();
			MDSLResource targetSpec = mot.moveOperation(moveSubject, "ExtractedEndpoint_" + moveSubject.getName()); // TODO (M) get from UI 

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
