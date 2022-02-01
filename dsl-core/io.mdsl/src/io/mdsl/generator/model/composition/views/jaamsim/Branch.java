package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.List;

public abstract class Branch extends Component {

	public static final String CHOICE_QUEUE_SUFFIX = "ChoiceQueue";
	
	public Branch(String name) {
		super(name);
	}
	
	public String getName() {
		return name;
	}

	public abstract List<String> getNextComponentList();

	protected String nextComponentNames(List<String> components) {
		String result = "";
		for(String component : components) {
			if(component!=null) {
				result += " " + component;
			}
			else {
				result += " n/a";
			}
		}
		return result;
	}
	
	public String dump() {
		String nameAndType = this.name + " (" + this.getClass().getSimpleName() + ")"; 
		String nextComponentNames = nextComponentNames(getNextComponentList());
				
		return nameAndType + ", nextComponents:" + nextComponentNames;
	}
}
