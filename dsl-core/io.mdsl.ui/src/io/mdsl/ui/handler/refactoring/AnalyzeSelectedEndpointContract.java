package io.mdsl.ui.handler.refactoring;

import org.eclipse.xtext.generator.IGenerator2;

// import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.Operation;
import io.mdsl.ui.handler.AbstractGenerationHandler;

import com.google.inject.Inject;

public class AnalyzeSelectedEndpointContract extends AbstractGenerationHandler {

	// not called, plugin.xml looks different for XText/MDSL handlers:
	@Inject
	private MoveOperationRefactoring generator;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		XtextEditor activeXtextEditor = org.eclipse.xtext.ui.editor.utils.EditorUtils.getActiveXtextEditor(event);
		ITextSelection selection = (ITextSelection) activeXtextEditor.getSelectionProvider().getSelection();

		EObjectAtOffsetHelper eObjectAtOffsetHelper = new EObjectAtOffsetHelper();

		EObject selectedElement = activeXtextEditor.getDocument().priorityReadOnly(xTextResource -> 
			eObjectAtOffsetHelper.resolveContainedElementAt(xTextResource, selection.getOffset())
		);

		MessageBox box = new MessageBox(activeXtextEditor.getShell(), SWT.OK);
		
		if(selectedElement instanceof EndpointContract) {
			EndpointContract ec = (EndpointContract) selectedElement;
			String endpointMessage = "The contract " + ec.getName()
					+  " has " + ec.getOps().size() + " operations.";
			System.out.println("Selected endpoint type: " + ec.getName());
			box.setMessage(endpointMessage);
		}
		else if(selectedElement instanceof Operation) {
			System.err.println("Operation selected: " + selectedElement.getClass());
			String endpointMessage = "Please select an endpoint type rather than an operation.";
			box.setMessage(endpointMessage);
		}
		else {
			System.err.println("Other element selected: " + selectedElement.getClass());
			String endpointMessage = "Please select an endpoint type to be analyzed.";
			box.setMessage(endpointMessage);
		}
		
		box.open();

		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	protected IGenerator2 getGenerator() {
		return generator;
	}
}
