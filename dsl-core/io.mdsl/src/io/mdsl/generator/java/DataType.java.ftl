<#assign dataType = genModel.dataTypes?filter(d -> d.name == dataTypeName)?first>
<#assign endpoint = genModel.endpoints?filter(d -> d.name == endpointName)?first>
<#assign hasLists = dataType.fields?filter(f -> f.isList())?has_content>
package ${resolveJavaPackage(genModel, endpoint)}.types;

<#if hasLists>
import java.util.List;
</#if>

/**
 * This class has been generated from the MDSL data type '${dataType.name}'. 
 * 
<#if dataType.name?starts_with("Anonymous")>
 * The name of this class has been generated because no name has been provided by the MDSL contract.
 * This typically happens when using nested types (nested parameter lists or trees) without giving the nested objects names.
 * TODO: You can fix this by providing a corresponding name in the MDSL contract (regenerate) or rename the class right here (use rename refactoring).
</#if>
 */
// TODO put Lombok annotations here (optionally/alternatively, can go to separate template)
public class ${mapType(dataType.name)} {

	<#if !dataType.fields?has_content>
	/** 
	 * TODO: This class has been generated out of an MDSL data type that did not contain any fields. 
	 *       Please specify the attributes of the type here.
	 */
	</#if>
	<#list dataType.fields as field>
	<#if field.name?starts_with("anonymous")>
	/** 
	 * The name of this field (${field.name}) has been generated because it is not defined in your MDSL contract. 
	 * TODO: You can fix this by providing an attribute name in MDSL (regenerate) or rename the field right here (use rename refactoring).
	 */
	</#if>
	private <#if field.isList()>List<</#if>${mapType(field.type.getName())}<#if field.isList()>></#if> ${field.name}; 
	</#list>
	
	<#list dataType.fields as field>
	public <#if field.isList()>List<</#if>${mapType(field.type.getName())}<#if field.isList()>></#if> get${capitalize(field.name)}() {
		return ${field.name};
	}
	
	public void set${capitalize(field.name)}(<#if field.isList()>List<</#if>${mapType(field.type.getName())}<#if field.isList()>></#if> ${field.name}) {
		this.${field.name} = ${field.name};
	}
	
	</#list>

}
