package io.mdsl.generator.model.composition.views.jaamsim;

import java.util.List;

import io.mdsl.generator.model.composition.Command;

public abstract class Duplicate extends Component {

	public Duplicate(String name) {
		super(name);
	}
	
	public Duplicate(String name, Command command) {
		super(name);
	}
	
	public abstract String getNextComponent();

	public abstract List<String> getTargetComponents();
	
	protected String elementNamesSeparatedWithBlank(List<String> elementNames) {
		String result = "";
		for(String element : elementNames) {
			if(element!=null) {
				result += " " + element;
			}
			else {
				result += " n/a";
			}
		}
		return result;
	}
	
	public String dump() {
		String nameAndType = this.name + " (" + this.getClass().getSimpleName() + ")"; 
		String targetComponentNames = this.elementNamesSeparatedWithBlank(getTargetComponents());
				
		return nameAndType + ", nextComponent: " + this.getNextComponent() + ", targetComponents:" + targetComponentNames;
	}
}
