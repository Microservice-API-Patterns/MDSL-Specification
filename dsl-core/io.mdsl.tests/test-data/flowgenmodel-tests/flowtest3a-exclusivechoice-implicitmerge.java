/*
 * This file is licensed to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package FlowTest3a;

// generated from orchestration flow part of flowtest3a-exclusivechoice-implicitmerge.mdsl
// The API description FlowTest3a features 1 orchestration flow(s) (a.k.a. API call sequences).

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.AggregationStrategy;

public class ExclusiveChoiceWithImplicitMerge extends RouteBuilder {
	class FlowInitiated {} 
	class FlowStep1CompletedOptionA {} 
	class FlowStep1CompletedOptionB {} 
	class FlowStep2Completed {} 
	class FlowStep3Completed {} 
	class FlowTerminated {} 

    class FlowStep1Processor implements Processor {
	    private String name;
	
        FlowStep1Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep2Processor implements Processor {
	    private String name;
	
        FlowStep2Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep3Processor implements Processor {
	    private String name;
	
        FlowStep3Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep4Processor implements Processor {
	    private String name;
	
        FlowStep4Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    public class JoinAggregatorStrategy implements AggregationStrategy {
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {    	
            if (oldExchange == null) {
        	    System.out.println("First time in JoinAggregatorStrategy: " + newExchange.getIn().getBody().toString());
        	    // newExchange.getIn().setBody(newExchange.getIn().getBody().toString());
                return newExchange;
            }

            System.out.println("In JoinAggregatorStrategy: (oldin) " + oldExchange.getIn().getBody().toString()); 
            System.out.println("In JoinAggregatorStrategy: (newin) " + newExchange.getIn().getBody().toString());
        		
            newExchange.getIn().setBody(oldExchange.getIn().getBody().toString() + "; " + newExchange.getIn().getBody().toString());
            return newExchange;
        }
    
        public String appendStrings(String message1, String message2) {    	
        	return message1 + message2;
        }
    }
    
    public static void main(String[] args) throws Exception {
        ExclusiveChoiceWithImplicitMerge builder = new ExclusiveChoiceWithImplicitMerge();
        builder.runExclusiveChoiceWithImplicitMerge();
    }
    
    public void runExclusiveChoiceWithImplicitMerge() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow ExclusiveChoiceWithImplicitMerge in FlowTest3a";
		template.sendBody("direct:FlowInitiated", testMessage);
        // TODO add header to message if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:FlowInitiated", testMessage, "ChoiceCondition", "choiceValue"); 
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:FlowInitiated").to("direct:FlowStep1"); // event to command
        from("direct:FlowStep1CompletedOptionA").to("direct:FlowStep2"); // event to command
        from("direct:FlowStep1CompletedOptionB").to("direct:FlowStep3"); // event to command
        from("direct:FlowStep2Completed").to("direct:FlowStep4"); // event to command
        from("direct:FlowStep3Completed").to("direct:FlowStep4"); // event to command
        // note: FlowTerminated does not invoke any commands 

        // routes for domain event production steps
        from("direct:FlowStep1").choice() // command to multiple events 
        .when(simple("${header.FlowStep1EventEmissionCondition} == 'choiceOfFlowStep1CompletedOptionA'")).to("direct:FlowStep1CompletedOptionA")
        .when(simple("${header.FlowStep1EventEmissionCondition} == 'choiceOfFlowStep1CompletedOptionB'")).to("direct:FlowStep1CompletedOptionB")
        .otherwise().to("mock:bye").stop();
        from("direct:FlowStep2").process(new FlowStep2Processor("FlowStep2")).to("direct:FlowStep2Completed"); // command to single event
        from("direct:FlowStep3").process(new FlowStep3Processor("FlowStep3")).to("direct:FlowStep3Completed"); // command to single event
        from("direct:FlowStep4").process(new FlowStep4Processor("FlowStep4")).to("direct:FlowTerminated"); // command to single event
        
        // routes to terminate flow
        from("direct:FlowTerminated").to("mock:bye").stop();
    }
}
