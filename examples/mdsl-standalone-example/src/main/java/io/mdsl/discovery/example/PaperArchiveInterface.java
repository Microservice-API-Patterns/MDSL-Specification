package io.mdsl.discovery.example;

import io.mdsl.annotations.ParameterClassifier;
import io.mdsl.annotations.ServiceEndpoint;
import io.mdsl.annotations.ServiceOperation;
import io.mdsl.annotations.ServiceParameter;

// @ServiceEndpoint()
// @ServiceEndpoint(role="")
@ServiceEndpoint(role="INFORMATION_HOLDER_RESOURCE") // overwritten in class-interface demo
public interface PaperArchiveInterface {
	// @ServiceOperation(primaryResponsibility = "")
	@ServiceOperation(responsibility = "STATE_CREATION_OPERATION", parameters=ParameterClassifier.all)
	String createNewPaperItem(String who, String what, String where);
	
	@ServiceOperation()
	PaperItemDTO lookupPaperItem(int doi);
}
