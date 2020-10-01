package io.mdsl.generator.asyncapi

import io.mdsl.generator.asyncapi.helpers.AsyncApiGeneratorHelper
import io.mdsl.generator.asyncapi.models.CardinalityDescription
import io.mdsl.generator.asyncapi.models.ParameterDescriptor
import java.util.ArrayList
import java.util.List
import io.mdsl.apiDescription.ParameterForest
import io.mdsl.apiDescription.ElementStructure
import io.mdsl.apiDescription.ParameterTree
import io.mdsl.apiDescription.AtomicParameterList
import io.mdsl.apiDescription.TypeReference
import io.mdsl.apiDescription.AtomicParameter
import io.mdsl.apiDescription.SingleParameterNode
import io.mdsl.apiDescription.GenericParameter
import io.mdsl.apiDescription.TreeNode

class AsyncApiDataTypeGenerator {
	
	int genericParameterNameCount = 1;
	
	def compile(ElementStructure dto) '''
		««« Single Parameter Node
		«dto.np?.compile»
		««« Parameter Tree
		«dto.pt?.compile»
		««« Parameter Forest
		«dto.pf?.compile»
		««« Atomic Parameter List
		«dto.apl?.compile»
	'''
	

	private def compile(ParameterForest pf) '''
		type: array
		items:
			«FOR tree : AsyncApiGeneratorHelper.getRootTrees(pf)»
				-
					«tree.compile»
			«ENDFOR»
	'''
	
	private def compile(ParameterTree pt) '''
		«IF AsyncApiGeneratorHelper.isArray(pt.card)»
			type: array
			items:
				type: object
				«insertRequiredProperties(getRequiredProperties(pt))»
				properties:
					«pt.compileTreeNoHead»
		«ELSE»
			type: object
			«insertRequiredProperties(getRequiredProperties(pt))»
			properties:
				«pt.compileTreeNoHead»
		«ENDIF»
		'''
		
		private def compileTreeNoHead(ParameterTree pt) '''
			«pt.first?.compile»
			«IF pt.nexttn !== null»
				«FOR treeNode : pt.nexttn»
					«treeNode.compile»
				«ENDFOR»
			«ENDIF»
		'''
	
	private def compile(TreeNode tn) '''
		«tn.apl?.compile»
		«tn.pn?.compileSingleParameterNodeOnlyProp»
		«IF tn.children !== null»
			«getNameOrPlaceholder(tn.children.name)»:
				«IF AsyncApiGeneratorHelper.isArray(tn.children.card)»
					«tn.children.compile»
				«ELSE»
					type: object
					«insertRequiredProperties(getRequiredProperties(tn.children))»
					properties:
						«tn.children.compileTreeNoHead»
				«ENDIF»
		«ENDIF»
	'''
	
	private def compile(AtomicParameterList apl) '''
		«IF AsyncApiGeneratorHelper.isArray(apl.card)»
		type: array
		items:
			type: object
			«insertRequiredProperties(getRequiredProperties(apl))»
			properties:
				«apl.first?.compile»
				«IF apl.nextap !== null»
					«FOR atomP : apl.nextap»
						«atomP.compile»
					«ENDFOR»
				«ENDIF»
		«ELSE»
		
		type: object
		«insertRequiredProperties(getRequiredProperties(apl))»
		properties:
			«apl.first?.compile»
			«IF apl.nextap !== null»
				«FOR atomP : apl.nextap»
					«atomP.compile»
				«ENDFOR»
			«ENDIF»
		«ENDIF»

	'''
	
	private def getRequiredProperties(AtomicParameterList apl){
		val parameters = new ArrayList()
		parameters.add(apl.first)
		parameters.addAll(apl.nextap)
		
		val requiredParams = parameters.map[p | p.rat.name] // TODO: handle genericParameterNameCount()
		
		for(p : parameters){
			if(p.card?.zeroOrMore !== null || p.card?.zeroOrOne !== null){
				// optional parameter
				requiredParams.removeIf(param | p.rat.name === param);
			}
		}
		
		return requiredParams;
	}
	
	private def getRequiredProperties(List<TreeNode> properties){
		val allProperties = properties.map[p | {
			if(p.apl !== null)
				return new ParameterDescriptor(p.apl.name, p.apl.card);
			
			if(p.pn !== null){
				if(p.pn.atomP !== null)
					return new ParameterDescriptor(p.pn.atomP.rat.name, p.pn.atomP.card); // TODO: handle genericParameterNameCount()
				if(p.pn.genP !== null)
					return new ParameterDescriptor(p.pn.genP.name, null); // genP does not have card?	
				if(p.pn.tr !== null)
					return new ParameterDescriptor(p.pn.tr.name, p.pn.tr.card);	
			}
			
			if(p.children !== null){
				return new ParameterDescriptor(p.children.name, p.children.card);		
			}
		}] 
		
		val requiredProperties = new ArrayList();
		for(p : allProperties){
			if(p.card?.zeroOrMore !== null || p.card?.zeroOrOne !== null){
				// optional parameter
			}else {
				requiredProperties.add(p.name)
			}
		}
		
		return requiredProperties
	}
	
	private def getRequiredProperties(ParameterTree pt){
		val properties = new ArrayList()
		properties.add(pt.first)
		properties.addAll(pt.nexttn)
		return getRequiredProperties(properties);
		
	}
	
	
	private def insertRequiredProperties(List<String> params)'''
		«IF params !== null && params.length > 0 && params.map[p | if(p !== null) p else ""].join().length > 0»
		required:
			«FOR p : params»
				«IF p !== null && p.length > 0»
				-  «p»
				«ENDIF»
			«ENDFOR»
		«ENDIF»
	'''
	
	private def compile(TypeReference tr) {
		return compile(tr,true);
	}
	
	def compile(TypeReference tr, boolean hasName) '''
		«IF hasName»
			«getNameOrPlaceholder(tr.name)»:
		«ENDIF»
			«IF AsyncApiGeneratorHelper.isArray(tr?.card)»
			type: array
			items:
				$ref: '#/components/schemas/«tr.dcref.name»'
			«ELSE»
			$ref: '#/components/schemas/«tr.dcref.name»'
			«ENDIF»
	'''
	
	private def getNameOrPlaceholder(String name){
		if(name !== null && name.length > 0)
			return name;
			
		return "unnamedParameter" + this.genericParameterNameCount++;
	}
	
	private def compile(AtomicParameter atomP) '''
		«getNameOrPlaceholder(atomP.rat.name)»:
			«IF AsyncApiGeneratorHelper.isArray(atomP?.card)»
			type: array
			items: 
				type: «getType(atomP)»
			«ELSE»
			type: «getType(atomP)»
			«ENDIF»
	'''
 
 	// TODO: handle genericParameterNameCount in required parameters
	private def compile(SingleParameterNode spn) '''
			«IF spn.tr === null»
				type: object
				«IF spn.genP !== null»
					«spn.genP?.compile»
				«ELSE»
					«IF spn?.atomP?.card?.zeroOrOne === null && spn?.atomP?.rat?.name !== null» 
					required:
						-  «spn.atomP.rat.name»
					«ENDIF»
					properties:
						«spn.atomP?.compile»
				«ENDIF»
				«ELSE»
				«spn.tr.compile»
			«ENDIF»
	'''
	
		private def compileSingleParameterNodeOnlyProp(SingleParameterNode spn) '''
			«spn.genP?.compile»
			«spn.atomP?.compile»
			«spn.tr?.compile»
	'''
	
	private def compile(GenericParameter genP) '''
			«getNameOrPlaceholder(genP?.name)»:
				type: object
		'''
	
	private def getType(AtomicParameter atomP){
		val type = getType(atomP.rat.btype);
		
		if(type !== null && type.length > 0)
			return type;
			
		return getTypeFromRole(atomP.rat.role);
	}
	
	def getType(String type){
		switch type{
			case 'int': return 'number'
			case 'double': return 'number'
			case 'long': return 'number'
			case 'bool': return 'boolean'
			default: return type 
		}
	}
	
	// TODO: is it possible to infer more info?
	private def getTypeFromRole(String role){
		return 'object';
	}
	
	
	def getRootPayloadCardinality(ElementStructure root){
		
		val cardDescription = new CardinalityDescription(false,false);
		
		if(AsyncApiGeneratorHelper.isArray(root?.apl?.card)){
			cardDescription.array = true;
			cardDescription.atLeastOne = root.apl.card.atLeastOne !== null;
		}
		
		if(AsyncApiGeneratorHelper.isArray(root?.np?.atomP?.card)){
			cardDescription.array = true;
			cardDescription.atLeastOne = root.np.atomP.card.atLeastOne !== null;
		}
			
		if(AsyncApiGeneratorHelper.isArray(root?.np?.tr?.card)){
			cardDescription.array = true;
			cardDescription.atLeastOne = root.np.tr.card.atLeastOne !== null;
		}
		 
		if(AsyncApiGeneratorHelper.isArray(root?.pf?.ptl?.first?.card)){
			cardDescription.array = true;
			cardDescription.atLeastOne = root.pf.ptl.first.card.atLeastOne !== null;
		}
		
		return cardDescription;
	}
}