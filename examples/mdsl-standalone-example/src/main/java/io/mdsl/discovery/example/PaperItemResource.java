package io.mdsl.discovery.example;

import io.mdsl.annotations.ServiceEndpoint;
import io.mdsl.annotations.ServiceOperation;

@ServiceEndpoint(role="INFORMATION_HOLDER_RESOURCE")
public interface PaperItemResource {
	@ServiceOperation(responsibility = "COMPUTATION_FUNCTION")
	String convertToMarkdown(String who, String what, String where);
}
