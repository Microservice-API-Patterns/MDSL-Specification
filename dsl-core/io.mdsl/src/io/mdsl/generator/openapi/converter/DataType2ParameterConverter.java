package io.mdsl.generator.openapi.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.Cardinality;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.GenericParameter;
import io.mdsl.apiDescription.HTTPParameter;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.SingleParameterNode;
import io.mdsl.apiDescription.TypeReference;
import io.mdsl.dsl.ServiceSpecificationAdapter;
import io.mdsl.exception.MDSLException;
import io.mdsl.utils.MDSLLogger;
import io.mdsl.utils.MDSLSpecificationWrapper;
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

	private static final String PARAMETER_PREFIX = "Parameter";
	private static final String ANONYMOUS_TREE_IDENTIFIER = "anonymousTree";
	private int anonymousParameterCounter = 1;
	private DataType2SchemaConverter schemaConverter;
	private MDSLSpecificationWrapper mdslResolver;

	public DataType2ParameterConverter(ServiceSpecificationAdapter mdslSpecification) {
		this.schemaConverter = new DataType2SchemaConverter();
		this.mdslResolver = new MDSLSpecificationWrapper(mdslSpecification);
	}
	
	public Parameter convertParameterTree(ParameterTree pt, HTTPParameter boundParameter, boolean externalCardinality) {
		if(pt==null) {
			return null;
		}
		
		Schema treeSchema = schemaConverter.convertAndCreateSchema4ParameterTreeAndItsNodes(pt, externalCardinality);
		// treeSchema = schemaConverter.mapCardinalities(pt.getCard(), treeSchema);
		
		String treeName;
		if(pt.getName()!=null)
			treeName = pt.getName();
		else {
			treeName = ANONYMOUS_TREE_IDENTIFIER;
		}
	
		Parameter deepObjectForTree = new Parameter().name(treeName).in(this.convertParameterTypeToOpenAPIValue(boundParameter));
		// note: uses full PT, does not work for individual parameter bindings to different types (known limitation)
		deepObjectForTree.schema(treeSchema);  
		
		if(boundParameter==HTTPParameter.QUERY) {
			deepObjectForTree.style(StyleEnum.DEEPOBJECT);
		}
		else if(boundParameter==HTTPParameter.PATH) {
			MDSLLogger.reportWarning("Mapping a PATH parameter to a deep object: " + pt.getName());
			deepObjectForTree.style(StyleEnum.DEEPOBJECT); // TODO not fully tested (matrix required?)
		}	
		else if (boundParameter==HTTPParameter.COOKIE) {
			// can the cookie be structured? matrix? OAS tools support?
			deepObjectForTree.style(StyleEnum.FORM);
		}
		// new case, added in V5.4
		else if (boundParameter==HTTPParameter.HEADER) {
			MDSLLogger.reportWarning("Mapping a HEADER parameter to to a deep object: " + pt.getName());
			// deepObjectForTree.style(StyleEnum.DEEPOBJECT); // OAS does not validate
			deepObjectForTree.style(StyleEnum.SIMPLE);
		}
		// new case, added in V5.4
		else if (boundParameter==HTTPParameter.BODY) {
			// BODY binding handled elsewhere (not going to parameter)
			MDSLLogger.reportError("Mapping a BODY parameter to style MATRIX: " + pt.getName());
			deepObjectForTree.style(StyleEnum.MATRIX);
		}
		// new case:
		else {
			MDSLLogger.reportError("Unexpected parameter type: " + boundParameter);
		}
		
		deepObjectForTree.explode(true);

		return deepObjectForTree;
	}

	private List<Parameter> convertAtomicParameterList(List<AtomicParameter> atomicParameters, HTTPParameter boundParameter) {
		return atomicParameters.stream().map(ap -> convertAtomicParameter(ap, boundParameter)).collect(Collectors.toList());
	} 
	
	private Parameter convertAtomicParameter(AtomicParameter atomicParameter, HTTPParameter parameterType) {
		RoleAndType roleAndType = atomicParameter.getRat();
		PatternStereotype classifier = atomicParameter.getClassifier(); // MAP decorator or other 
		
		String parameterDescription;  
		parameterDescription = MDSLSpecificationWrapper.getClassifierAndElementStereotype(classifier, roleAndType);
		
		// check and respect '?' cardinality (as well as '*' and '+')
		boolean required = findOutWhetherParameterIsRequired(atomicParameter);
		
		String inValue = this.convertParameterTypeToOpenAPIValue(parameterType);
		
		// TODO (future work) do more for cookie and header here; same for path (see below)?
		
		Parameter result = null;
		Cardinality card = atomicParameter.getCard();
		// note: this cardinality check differs from that in findOutWhetherParameterIsRequired
		if(card != null && (card.getZeroOrOne()!=null || card.getAtLeastOne()!=null)) {
			// handle array (for atomic parameters)			
			ArraySchema arraySchema = new ArraySchema();
			Schema<?> schema = schemaConverter.convert(atomicParameter);
			if(schema!=null) {
				arraySchema.items(schema);
				result = new Parameter().name(getUniqueName(roleAndType.getName()))
						.in(inValue).description(parameterDescription) // not needed twice?
						.required(required).schema(arraySchema);
		}
		}
		else {
			Schema<?> schema = schemaConverter.convert(atomicParameter);
			if(schema!=null) {
				result = new Parameter().name(getUniqueName(roleAndType.getName()))
						.in(inValue).description(parameterDescription)
						.required(required).schema(schema);
			}
		}
		return result;
	}

	private boolean findOutWhetherParameterIsRequired(AtomicParameter atomicParameter) {
		boolean required = true;
		Cardinality card = atomicParameter.getCard();
		if(card != null && (card.getZeroOrOne()!=null || card.getZeroOrMore()!=null)) {
			required=false;
		}
		return required;
	}
	
	public Parameter convertGenericParameter(GenericParameter genP, HTTPParameter paramType) {
		String parameterDescription; 
		parameterDescription = "Generic Parameter " + genP.getName();
		
		//  note: no classifier, no cardinality in GPs

		String inValue = this.convertParameterTypeToOpenAPIValue(paramType);
				
		Parameter result = null;
		Schema<?> schema = schemaConverter.convert(genP);
		if(schema!=null) {
			result = new Parameter().name(getUniqueName(genP.getName()))
					.in(inValue).description(parameterDescription).schema(schema);
			result.setDescription(parameterDescription);
		}
		return result;
	}
	
	private Parameter convertTypeReference(TypeReference tr, HTTPParameter parameterBinding) {
		String parameterDescription; ; 
		PatternStereotype classifier = tr.getClassifier(); // MAP decorator or other 
		parameterDescription = "Type reference " + tr.getName();
		
		//  note: no classifier, no cardinality in GPs

		String inValue = this.convertParameterTypeToOpenAPIValue(parameterBinding);
		
		Parameter result = null;
		Schema<?> schema = schemaConverter.createSchemaForTypeReference(tr);
		if(schema!=null) {
			result = new Parameter().name(getUniqueName(tr.getName()))
					.in(inValue).description(parameterDescription).schema(schema);
			result.setDescription(parameterDescription);
		}
		return result;
	}

	public String convertParameterTypeToOpenAPIValue(HTTPParameter parameter) {
		if(parameter==null) {
			MDSLLogger.reportWarning("Returning a parameter in value of 'query' although parameter is not bound (null value of parameter)");
			return "query";
			// return null;
		}
		if (parameter == HTTPParameter.QUERY)
			return "query";
		if (parameter == HTTPParameter.HEADER)
			return "header";
		if (parameter == HTTPParameter.PATH)
			return "path";
		if (parameter == HTTPParameter.COOKIE)
			return "cookie";
		if (parameter == HTTPParameter.BODY) // not defined in OAS
			throw new MDSLException("BODY is an unsupported mapping type"); // not going to validate in OAS tools and libraries
		throw new MDSLException("Unsupported mapping type " + parameter.getLiteral()); // can't get here
	}

	private String getUniqueName(String originalName) {
		if (originalName != null && !"".equals(originalName))
			return originalName;

		String anonymousName = PARAMETER_PREFIX + anonymousParameterCounter;
		anonymousParameterCounter++;
		return anonymousName;
	}
	
	public List<Parameter> convertSingleRepresentationElementToOneOrMoreParameters(ElementStructure structure, HTTPParameter boundParameter) {
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
	
		// find out whether we deal with a simple or a complex parameter
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			// find referenced explicit type and call same method again (rather deep navigation!)
			return convertSingleRepresentationElementToOneOrMoreParameters(structure.getNp().getTr().getDcref().getStructure(), boundParameter);
		} else if (structure.getPt() != null) {
			if(mdslResolver.isParameterTreeAtomic(structure.getPt()))
				atomicParameterList.addAll(mdslResolver.collectAtomicParameters(structure.getPt()));
			else {
				List<Parameter> pl = new ArrayList<Parameter>();
				// map complex tree as OAS deepObject (not possible for header, cookie, path?) 
				pl.add(convertParameterTree(structure.getPt(), boundParameter, false));
				return pl;
			}
		} else {
			throw new MDSLException("Parameter cannot be mapped, please simplify or extend it."); // no PF, for instance, TODO no "idOnly"; not sure about 'P'
		}
		
		// iterate through first level if flat structure (AP, APL, PT of depth 1):
		return convertAtomicParameterList(atomicParameterList, boundParameter);
	}
	
	public List<Parameter> convertAtomicParameterToOneParameter(AtomicParameter ap, HTTPParameter parameterBinding) {
		List<Parameter> result = new ArrayList<Parameter>(); 
		Parameter convertedAtom = convertAtomicParameter(ap, parameterBinding);
		if(convertedAtom!=null) {
			result.add(convertedAtom);
		}
		return result;
	}
	
	public List<Parameter> convertSingleParameterNodeToOneParameter(SingleParameterNode spn, HTTPParameter parameterBinding) {
		List<Parameter> result = new ArrayList<Parameter>(); 
		if(spn.getAtomP()!= null) {
			Parameter convertedAtom = convertAtomicParameter(spn.getAtomP(), parameterBinding);
			if(convertedAtom!=null) {
				result.add(convertedAtom);
			}
			return result;
		}
		else if(spn.getGenP()!= null) {
			Parameter convertedAtom = convertGenericParameter(spn.getGenP(), parameterBinding);
			if(convertedAtom!=null) {
				result.add(convertedAtom);
			}
			return result;
		}
		else if(spn.getTr()!= null) {
			// ElementStructure referencedTypeStructure = spn.getTr().getDcref().getStructure();
			// return convertSingleRepresentationElementToOneOrMoreParameters(referencedTypeStructure, parameterBinding);
			Parameter convertedAtom = convertTypeReference(spn.getTr(), parameterBinding);
			result.add(convertedAtom);
			return result;
		}
		else {
			return null;
		}
	}
}
