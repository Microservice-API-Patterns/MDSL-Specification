package io.mdsl.generator.model.composition.views.jaamsim;

public class Queue extends Component {
	
	private static final String DEFAULT_MAX_VALID_LENGTH = "100000000";
	private String maxValidLength = DEFAULT_MAX_VALID_LENGTH;

	public Queue(String name) {
		super(name);
	}
	
	public void setMaxValidLength(String maxValidLength) {
		this.maxValidLength = maxValidLength;
	}
	
	public String getMaxValidLength() {
		return maxValidLength;
	}

	public String dump() {
		return name;
	}
}
