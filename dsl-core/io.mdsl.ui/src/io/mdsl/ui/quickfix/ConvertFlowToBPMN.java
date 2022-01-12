package io.mdsl.ui.quickfix;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.program.Program;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;

import io.mdsl.apiDescription.Orchestration;
import io.mdsl.generator.bpmn.SketchMinerLinkCreator;
import io.mdsl.utils.MDSLLogger;;

class ConvertFlowToBPMN extends QuickfixSemanticModification {
	@Override
	public void performQuickfix(EObject element, IModificationContext context) {
		Orchestration flow = (Orchestration) element;
		SketchMinerLinkCreator smlc = new SketchMinerLinkCreator();
		String smStoryAsLink = smlc.createSketchMinerLink(flow);
		MDSLLogger.reportDetailedInformation(smStoryAsLink);
		// open browser with BPMN story link 
		Program.launch(smStoryAsLink);
	}
}