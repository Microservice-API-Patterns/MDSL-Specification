package io.mdsl.discovery.example;

import io.mdsl.annotations.ServiceEndpoint;
import io.mdsl.annotations.ServiceOperation;

// @Deprecated() // just to test non-MDSL annotation presence
@ServiceEndpoint(role="OPERATIONAL_DATA_HOLDER")
public class PaperArchiveClass extends SampleRepository implements PaperArchiveInterface  {

	@Override
	@ServiceOperation(responsibility = "STATE_CREATION_OPERATION")
	public String createNewPaperItem(String who, String what, String where) {
		return "Hi";
	}

	// test -a(all) option by commenting out @ServiceOperation():
	@ServiceOperation()
	public PaperItemDTO lookupPaperItem(int doi) {
		utility("n/a");
		return null;
	}
	
	@Deprecated
	// @ServiceOperation()
	private void utility(String in) {}
}
