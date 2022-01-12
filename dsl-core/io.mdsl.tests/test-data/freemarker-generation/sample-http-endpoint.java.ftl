package io.mdsl.web.interfaces;
// TODO adjust package name as required for your setup 
// package ${genModel.apiName};

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

<#list genModel.providers as provider>
<#if provider?has_content>
// TODO add this to Spring configuration (unless auto discovery is enabled):
/*
	@Autowired 
	private ${provider.name}WebController a${provider.name}WebControllerBean;
*/
<#-- TODO report "no provider case", also generate for endpoint-only MDSL -->
@Controller
public class ${provider.name}WebController {	
	<#list provider.offeredEndpoints as contract><#list contract.operations as operation> 
    <#-- TODO this only works if there is one and only one HTTP binding with one and only one resource: -->
    /*
    The operation ${operation.name} can be tested with:
    // get/delete parameter names and sample data according to binding (cookie, path, query parameters) from genmodel (not yet complete)
    curl -X ${contract.protocolBinding.resources[0].getHTTPMethodName4Operation(operation.name)?upper_case} "${contract.protocolBinding.uriPath}/${contract.name}${contract.protocolBinding.resources[0].uriPath}" -H  "accept: application/json" -H  "Content-Type: application/json"  -d "${operation.request.sampleJSON(1)}"
    */
    @${contract.protocolBinding.resources[0].getHTTPMethodName4Operation(operation.name)}Mapping(value="/${contract.name}${contract.protocolBinding.resources[0].uriPath}", consumes="application/json", produces="application/json")
    public ResponseEntity<String> ${operation.name}(
      @RequestBody String inData 
    ) {
      System.out.println("Received expected request message payload: " + inData.toString());
      <#-- // inject JSON sample data gen call from genmodel here, TODO (?) add top-level parameter identifier from MDSL (genmodel?) -->
      // note that request/response DTOs for Jackson processing are available via Java modulith generator (multiple files in .zip)
      return new ResponseEntity<String>("${operation.response.sampleJSONWithEscapedQuotes(1)}", HttpStatus.OK);
    }
  <#--@GetMapping(value="/${contract.name}", produces="application/json")
    public ResponseEntity<String> ${operation.name}ResponseViaGet() {
      // TODO inject JSON sample data gen call from genmodel here
      return new ResponseEntity<String>("{\"NestedTree\": { \"_metadata\": \"unknown\", \"p1\": [\"someText\"], \"p2\":\"true\", \"subtree\":{ \"_metadata\": \"unknown\", \"p31\":\"42\", \"p32\":\"TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu\"}}}", HttpStatus.OK);
    }
  -->
  </#list>
  </#list>
}
</#if>
</#list>