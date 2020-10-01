package io.mdsl.generator.openapi.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.mdsl.apiDescription.AtomicParameter;
import io.mdsl.apiDescription.ElementStructure;
import io.mdsl.apiDescription.ParameterTree;
import io.mdsl.apiDescription.PatternStereotype;
import io.mdsl.apiDescription.RoleAndType;
import io.mdsl.apiDescription.TreeNode;
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
	private PatternStereotype classifier;

	public DataType2ParameterConverter() {
		this.schemaConverter = new DataType2SchemaConverter();
	}

	// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#parameterObject

	/**
	 * handles only two cases for now: - atomic parameter list - single atomic
	 * parameter
	 */
	public List<Parameter> convert(ElementStructure structure, String operationName) {
		List<AtomicParameter> atomicParameterList = Lists.newLinkedList();
		if (structure.getApl() != null) {
			atomicParameterList.add(structure.getApl().getFirst());
			atomicParameterList.addAll(structure.getApl().getNextap());
		} else if (structure.getNp() != null && structure.getNp().getAtomP() != null) {
			atomicParameterList.add(structure.getNp().getAtomP());
		} else if (structure.getNp() != null && structure.getNp().getTr() != null) {
			return convert(structure.getNp().getTr().getDcref().getStructure(), operationName);
		} else if (structure.getPt() != null && isParameterTreeAtomic(structure.getPt())) {
			atomicParameterList.addAll(collectAtomicParameters(structure.getPt()));
		} else {
			// TODO (v4.1): use new HTTP binding to also map simple PTs
			throw new MDSLException("Parameter in operation '" + operationName + "' must be atomic or list of atoms, no trees or forests with external types please.");
		}
		return convert(atomicParameterList);
	}

	private List<Parameter> convert(List<AtomicParameter> atomicParameters) {
		return atomicParameters.stream().map(p -> convert(p)).collect(Collectors.toList());
	}

	private Parameter convert(AtomicParameter atomicParameter) {
		RoleAndType roleAndType = atomicParameter.getRat();
		classifier = atomicParameter.getClassifier();
		String descr = "unspecified";

		if (classifier != null) {
			String p = classifier.getPattern();
			if (p != null && !p.isEmpty())
				descr = p;
			else {
				p = classifier.getEip();
				if (p != null && !p.isEmpty())
					descr = p;
				else {
					p = classifier.getName();
					if (p != null && !p.isEmpty())
						descr = p;
				}
			}
		}

		// TODO (high prio) path vs. query vs. cookie (from HTTP binding), path not
		// working yet
		return new Parameter().name(getUniqueName(roleAndType.getName())).in("query").description(descr).required(true).schema(schemaConverter.convert(atomicParameter));
	}

	private String getUniqueName(String originalName) {
		if (originalName != null && !"".equals(originalName))
			return originalName;

		String anonymousName = "Parameter" + anonymousParameterCounter;
		anonymousParameterCounter++;
		return anonymousName;
	}

	private boolean isParameterTreeAtomic(ParameterTree tree) {
		List<TreeNode> nodes = getTreeNodes(tree);
		for (TreeNode node : nodes) {
			if (node.getPn() != null && node.getPn().getAtomP() != null)
				continue;
			if (node.getApl() != null)
				continue;
			return false;
		}
		return true;
	}

	private List<AtomicParameter> collectAtomicParameters(ParameterTree tree) {
		List<AtomicParameter> list = Lists.newLinkedList();
		List<TreeNode> nodes = getTreeNodes(tree);
		for (TreeNode node : nodes) {
			if (node.getPn() != null && node.getPn().getAtomP() != null)
				list.add(node.getPn().getAtomP());
			if (node.getApl() != null) {
				list.add(node.getApl().getFirst());
				list.addAll(node.getApl().getNextap());
			}
		}
		return list;
	}

	private List<TreeNode> getTreeNodes(ParameterTree tree) {
		List<TreeNode> nodes = Lists.newLinkedList();
		nodes.add(tree.getFirst());
		nodes.addAll(tree.getNexttn());
		return nodes;
	}

}
