package io.mdsl.generator.model.composition.views.jaamsim;

import io.mdsl.generator.model.composition.Flow;

public class Statistics extends Component {

	private Flow flow;

	public Statistics(String name, Flow flow) {
		super(name);
		this.flow = flow;
	}
	
	public String getNextComponent() {
		return this.flow.getName() + "EntitySink";
	}

	public String dump() {
		return name;
	}
}
