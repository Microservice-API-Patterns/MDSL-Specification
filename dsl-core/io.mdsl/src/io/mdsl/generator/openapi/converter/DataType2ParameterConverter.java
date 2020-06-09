package io.mdsl.generator.openapi.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.atomicParameter;
import io.mdsl.apiDescription.elementStructure;
import io.mdsl.apiDescription.patternStereotype;
import io.mdsl.apiDescription.roleAndType;
import io.mdsl.exception.MDSLException;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Converts an MDSL datatype (elementStructure) to parameters
 * 
 * @author ska, zio
 *
 */
public class DataType2ParameterConverter {

	private int anonymousParameterCounter = 1;
	private DataType2SchemaConverter schemaConverter;
	private patternStereotype classifier;

	public DataType2ParameterConverter() {
		this.schemaConverter = new DataType2SchemaConverter();
	}
	
	// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#parameterObject

	/**
	 * handles only two cases for now: 
	 * - atomic parameter list 
	 * - single atomic parameter
	 */
	public List<Parameter> convert(elementStructure structure) {
		List<atomicParameter> atomicParameterList = Lists.newLinkedList();
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		}
		else {
			throw new MDSLException("Parameter must be atomic or list of atoms, no trees or forests please.");
		}
		return convert(atomicParameterList);
	}

	private List<Parameter> convert(List<atomicParameter> atomicParameters) {
		return atomicParameters.stream().map(p -> convert(p)).collect(Collectors.toList());
	}

	private Parameter convert(atomicParameter atomicParameter) {
		roleAndType roleAndType = atomicParameter.getRat();
		classifier = atomicParameter.getClassifier();
		String descr = "unspecified";
		
		if(classifier!=null) { 
			String p = classifier.getPattern(); 
			if(p!=null&&!p.isEmpty())
				descr=p;
			else {
				p = classifier.getEip();
				if(p!=null&&!p.isEmpty())
					descr=p;
				else {
					p=classifier.getName();
					if(p!=null&&!p.isEmpty())
					descr=p;
				}
			}
		}
		
		// TODO (high prio) path vs. query vs. cookie (from HTTP binding), path not working yet
		return new Parameter().name(getUniqueName(roleAndType.getName())).in("query").description(descr).required(true).schema(schemaConverter.convert(atomicParameter));
	}

	private String getUniqueName(String originalName) {
		if (originalName != null && !"".equals(originalName))
			return originalName;

		String anonymousName = "Parameter" + anonymousParameterCounter;
		anonymousParameterCounter++;
		return anonymousName;
	}

}
