package io.mdsl.transformations;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.mdsl.apiDescription.Action;
import io.mdsl.apiDescription.ApiDescriptionFactory;
import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.DataTransferRepresentation;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.IntegrationStory;
import io.mdsl.apiDescription.Operation;
import io.mdsl.apiDescription.OperationResponsibility;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.StoryObject;
import io.mdsl.apiDescription.TreeNode;
import io.mdsl.apiDescription.TypeReference;

public class ScenarioTransformationHelpers {
	
	public static String getFirstObjectName(IntegrationStory story) {
		if(story.getAction().getTarget()!=null)
			return story.getAction().getTarget();
		
		if(story.getOn()==null)
			return "AnonymousDTO";
		else if (story.getOn().size()>0)
			return story.getOn().get(0).getObject();
		else
			return "AnonymousDTO";
	}
	
	public static String getActionName(Action action, String name) {
		if(action.getPlainAction()!=null)
			if(name!=null && !name.equals("AnonymousDTO"))
				return action.getPlainAction() + name;
			else
				return action.getPlainAction();
		if(action.getKeyword()!=null)
			return DataTypeTransformationHelpers.replaceSpacesWithUnderscores(action.getKeyword().getName() + action.getTarget());
		return "";
	}
	
	public static DataTransferRepresentation createDTOForTargetAndObjects(IntegrationStory story, boolean hasSetCardinality) {
		DataTransferRepresentation dtr = ApiDescriptionFactory.eINSTANCE.createDataTransferRepresentation();
		
		ElementStructure es = createElementStructureDTO(story, hasSetCardinality);
		
		dtr.setPayload(es);
		return dtr;
	}

	public static ElementStructure createElementStructureDTO(IntegrationStory story, boolean hasSetCardinality) {
		boolean first=true;
		ParameterTree pt = ApiDescriptionFactory.eINSTANCE.createParameterTree();
		TreeNode tn;
		AtomicParameter ap;

		EList<StoryObject> on = story.getOn();
		
		if(story.getAction().getTarget()!=null) {
			ap = DataTypeTransformationHelpers.createAtomicDataParameter(DataTypeTransformationHelpers.decapitalizeName(story.getAction().getTarget()), null); // TODO tbd type
			tn = DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(ap);
			pt.setFirst(tn);
			first=false;
		}
		
		if(hasSetCardinality) {
			Cardinality setCard = ApiDescriptionFactory.eINSTANCE.createCardinality();
			setCard.setZeroOrMore("*");
			pt.setCard(setCard);
		}
			
		for(StoryObject object : on) {
			ap = DataTypeTransformationHelpers.createAtomicDataParameter(DataTypeTransformationHelpers.decapitalizeName(object.getObject()), null); // TODO tbd type
			tn = DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(ap);
			if(first) {
				pt.setFirst(tn);
				first=false;
			}
			else
				pt.getNexttn().add(tn);
		}
		
		if(first) {
			// no object present (plain action/no keyword action and no other objects present), so adding a dummy/default one
			ap = DataTypeTransformationHelpers.createAtomicDataParameter("dataTransferObject", null); // TODO tbd type
			tn = DataTypeTransformationHelpers.wrapAtomicParameterAsTreeNode(ap);
			pt.setFirst(tn);
		}
		
		ElementStructure es = ApiDescriptionFactory.eINSTANCE.createElementStructure();
		es.setPt(pt);
		return es;
	}
	
	public static  Operation createRetrievalOperation(IntegrationStory story, String opName, TypeReference tref) {
		Operation rop = TransformationHelpers.createOperationWithAtomicParameters(opName, "D", "string", true, "string");
		DataTransferRepresentation dtrDto = TransformationHelpers.createDTRFromTypeReference(tref);
		
		rop.getRequestMessage().getPayload().getNp().getAtomP().getRat().setName("queryFilter");		
		
		rop.setResponseMessage(dtrDto);
		rop.getResponseMessage().getPayload().getNp().getTr().setName("resultSet");
		
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setRo(opName);
		rop.setResponsibility(or);
		
		return rop;
	}
	
	public static Operation createUpdateOperation(IntegrationStory story, String opName, TypeReference tref) {
		Operation uop = TransformationHelpers.createOperationWithAtomicParameters(opName, "D", ""
				+ "string", true, "string");
			DataTransferRepresentation dtrDto = TransformationHelpers.createDTRFromTypeReference(tref);

		uop.setRequestMessage(dtrDto);
		uop.getRequestMessage().getPayload().getNp().getTr().setName("changeRequest");	
		
		uop.setResponseMessage(EcoreUtil.copy(dtrDto));
		uop.getResponseMessage().getPayload().getNp().getTr().setName("updateResult");
		
		OperationResponsibility or = ApiDescriptionFactory.eINSTANCE.createOperationResponsibility();
		or.setSto(opName);
		uop.setResponsibility(or);
		
		return uop;
	}
}
