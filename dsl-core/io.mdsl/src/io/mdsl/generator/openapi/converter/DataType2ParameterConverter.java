package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MAPLinkResolver;
import io.mdsl.utils.MDSLSpecificationWrapper;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

/**
 * Converts an MDSL datatype (elementStructure) to parameters
 * 
 * @author ska, socadk
 *
 */
public class DataType2ParameterConverter {

	private int anonymousParameterCounter = 1;
	private DataType2SchemaConverter schemaConverter;
	private PatternStereotype classifier; // TODO does this have to be an instance variable?
	private MDSLSpecificationWrapper mdslResolver;

	public DataType2ParameterConverter(ServiceSpecificationAdapter mdslSpecification) {
		this.schemaConverter = new DataType2SchemaConverter();
		this.mdslResolver = new MDSLSpecificationWrapper(mdslSpecification);
	}
	
	// TODO map MAP decorators such as <<Wish_List>>

	public Parameter convertTree(ParameterTree pt, HttpMethod verb, HTTPParameter boundParameter, boolean externalCardinality) {
		
		Schema treeSchema = schemaConverter.convertAndCreateSchema4TreeNode(pt, externalCardinality);
		// treeSchema = schemaConverter.mapCardinalities(pt.getCard(), treeSchema);
		
		String treeName;
		if(pt.getName()!=null)
			treeName = pt.getName();
		else {
			treeName = "anonymousTree";
		}
	
		Parameter deepObjectForTree = new Parameter().name(treeName).in(this.convertParameterTypeToOpenAPIValue(boundParameter));
		deepObjectForTree.schema(treeSchema);
		
		// TODO might have to do more for header type here; same for path type (matrix style?)		
		if(boundParameter==HTTPParameter.QUERY)
			deepObjectForTree.style(StyleEnum.DEEPOBJECT);
		else if (boundParameter==HTTPParameter.COOKIE)
			deepObjectForTree.style(StyleEnum.FORM);
		
		deepObjectForTree.explode(true);

		return deepObjectForTree;
	}

	private List<Parameter> convertAPL(List<AtomicParameter> atomicParameters, HttpMethod verb, HTTPParameter boundParameter) {
		return atomicParameters.stream().map(ap -> convertAP(ap, verb, boundParameter)).collect(Collectors.toList());
	} 
	
	private Parameter convertAP(AtomicParameter atomicParameter, HttpMethod verb, HTTPParameter paramType) {
		RoleAndType roleAndType = atomicParameter.getRat();
		this.classifier = atomicParameter.getClassifier();
		String descr = "unspecified";

		descr = getClassifier(roleAndType);
		
		// check and respect '?' cardinality 
		boolean required = true;
		Cardinality card = atomicParameter.getCard();
		if(card != null && (card.getZeroOrOne()!=null || card.getZeroOrMore()!=null)) {
			required=false;
		}
		
		String inValue = this.convertParameterTypeToOpenAPIValue(paramType);
		
		// TODO (tbc) do more for cookie and header here; same for path (see below)?
		
		Parameter result = null;
		if(card != null && (card.getZeroOrOne()!=null || card.getAtLeastOne()!=null)) {
			// handle array (for atomic parameters)			
			ArraySchema arraySchema = new ArraySchema();
			arraySchema.items(schemaConverter.convert(atomicParameter));
			result = new Parameter().name(getUniqueName(roleAndType.getName()))
			.in(inValue).description(descr) // not needed twice?
			.required(required).schema(arraySchema);
		}
		else {
			result = new Parameter().name(getUniqueName(roleAndType.getName()))
			.in(inValue).description(descr)
			.required(required).schema(schemaConverter.convert(atomicParameter));
		}
		return result;
	}

	private String getClassifier(RoleAndType roleAndType) {
		String result = null;
		if (this.classifier != null) {
			String p = classifier.getPattern();
			if (p != null && !p.isEmpty())
				result = p;
			else {
				p = classifier.getEip();
				if (p != null && !p.isEmpty())
					result = p;
				else {
					p = classifier.getName();
					if (p != null && !p.isEmpty())
						result = p;
				}
			}
		}
		if (result!=null&&result.equals("unspecified")) {
			result = MAPLinkResolver.mapParameterRoleAndType(roleAndType);
		}
		return result;
	}

	public String convertParameterTypeToOpenAPIValue(HTTPParameter parameter) {
		if(parameter==null)
			return null;
		if (parameter == HTTPParameter.QUERY)
			return "query";
		if (parameter == HTTPParameter.HEADER)
			return "header";
		if (parameter == HTTPParameter.PATH)
			return "path";
		if (parameter == HTTPParameter.COOKIE)
			return "cookie";
		if (parameter == HTTPParameter.BODY) // not defined in OAS
			throw new MDSLException("BODY is an unsupported mapping type"); // this is not going to validate in OAS tools and libraries
		throw new MDSLException("Unsupported mapping type"); // can't get here
	}

	private String getUniqueName(String originalName) {
		if (originalName != null && !"".equals(originalName))
			return originalName;

		String anonymousName = "Parameter" + anonymousParameterCounter;
		anonymousParameterCounter++;
		return anonymousName;
	}
	
	public List<Parameter> convertSingleRepresentationElementToOneOrMoreParameters(ElementStructure structure, HttpMethod verb, HTTPParameter boundParameter) {
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
	
		// find out whether we deal with a simple or a complex parameter
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again (rather deep navigation!)
			return convertSingleRepresentationElementToOneOrMoreParameters(structure.getNp().getTr().getDcref().getStructure(), verb, boundParameter);
		} else if (structure.getPt() != null) {
			if(mdslResolver.isParameterTreeAtomic(structure.getPt()))
				atomicParameterList.addAll(mdslResolver.collectAtomicParameters(structure.getPt()));
			else {
				List<Parameter> pl = new ArrayList<Parameter>();
				// map complex tree as OAS deepObject (not possible for header, cookie, path?) 
				pl.add(convertTree(structure.getPt(), verb, boundParameter, false));
				return pl;
			}
		} else {
			throw new MDSLException("Parameter cannot be mapped, please simplify or extend it."); // no PF, for instance, TODO no "idOnly"; not sure about 'P'
		}
		
		// iterate through first level if flat structure (AP, APL, PT of depth 1):
		return convertAPL(atomicParameterList, verb, boundParameter);
	}
	
	public List<Parameter> convertSingleRepresentationElementToOneParameter(AtomicParameter ap, HttpMethod verb, HTTPParameter boundParameter) {
		List<Parameter> result = new ArrayList<Parameter>(); 
		// not sure list is needed
		result.add(convertAP(ap, verb, boundParameter));
		return result;
	}
}
