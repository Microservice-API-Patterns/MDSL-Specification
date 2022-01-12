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
 
package FlowTest7;

// generated from orchestration flow part of flowtest7-branchingoptions-withnesting.mdsl
// The API description FlowTest7 features 2 orchestration flow(s) (a.k.a. API call sequences).

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.AggregationStrategy;

public class NestedFlow1 extends RouteBuilder {
	class FlowInitiated {} 
	class FlowStep1OutEvent1 {} 
	class FlowStep1OutEvent2 {} 
	class AND_FlowStep1OutEvent1_FlowStep1OutEvent2 {} 
	class FlowStep2Completed {} 
	class FlowStep3Completed {} 
	class FlowStep4Completed {} 
	class FlowStep5Completed {} 
	class AND_FlowStep3Completed_FlowStep4Completed {} 
	class FlowStep6Completed {} 
	class AND_FlowStep5Completed_FlowStep6Completed {} 
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

    class AND_FlowStep1_FlowStep2Processor implements Processor {
	    private String name;
	
        AND_FlowStep1_FlowStep2Processor(String name) {
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

    class FlowStep5Processor implements Processor {
	    private String name;
	
        FlowStep5Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep6Processor implements Processor {
	    private String name;
	
        FlowStep6Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep7Processor implements Processor {
	    private String name;
	
        FlowStep7Processor(String name) {
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
        NestedFlow1 builder = new NestedFlow1();
        builder.runNestedFlow1();
    }
    
    public void runNestedFlow1() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow NestedFlow1 in FlowTest7";
		template.sendBody("direct:FlowInitiated", testMessage);
        // TODO add header to message if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:FlowInitiated", testMessage, "ChoiceCondition", "choiceValue"); 
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:FlowInitiated").to("direct:AND_FlowStep1_FlowStep2"); // event to composite command (recipient list)
        from("direct:AND_FlowStep1_FlowStep2").process(new AND_FlowStep1_FlowStep2Processor("AND_FlowStep1_FlowStep2")).recipientList(constant("direct:FlowStep1, direct:FlowStep2")).parallelProcessing(); 
        from("direct:FlowStep1OutEvent1").to("direct:FlowStep3"); // event to command
        from("direct:FlowStep1OutEvent2").to("direct:FlowStep4"); // event to command
        // note: AND_FlowStep1OutEvent1_FlowStep1OutEvent2 does not invoke any commands 
        from("direct:FlowStep2Completed").to("direct:FlowStep5"); // event to command
        // note: FlowStep3Completed does not invoke any commands 
        // note: FlowStep4Completed does not invoke any commands 
        // note: FlowStep5Completed does not invoke any commands 
        from("direct:FlowStep3Completed").to("direct:AND_FlowStep3Completed_FlowStep4Completed:AggregatorIn");
        from("direct:FlowStep4Completed").to("direct:AND_FlowStep3Completed_FlowStep4Completed:AggregatorIn");
        from("direct:AND_FlowStep3Completed_FlowStep4Completed:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(2).to("direct:FlowStep6");
        // note: FlowStep6Completed does not invoke any commands 
        from("direct:FlowStep5Completed").to("direct:AND_FlowStep5Completed_FlowStep6Completed:AggregatorIn");
        from("direct:FlowStep6Completed").to("direct:AND_FlowStep5Completed_FlowStep6Completed:AggregatorIn");
        from("direct:AND_FlowStep5Completed_FlowStep6Completed:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(2).to("direct:FlowStep7");
        // note: FlowTerminated does not invoke any commands 

        // routes for domain event production steps
        from("direct:FlowStep1").process(new FlowStep1Processor("FlowStep1")).recipientList(constant("direct:FlowStep1OutEvent1, direct:FlowStep1OutEvent2")).parallelProcessing(); 
        from("direct:FlowStep2").process(new FlowStep2Processor("FlowStep2")).to("direct:FlowStep2Completed"); // command to single event
        from("direct:FlowStep3").process(new FlowStep3Processor("FlowStep3")).to("direct:FlowStep3Completed"); // command to single event
        from("direct:FlowStep4").process(new FlowStep4Processor("FlowStep4")).to("direct:FlowStep4Completed"); // command to single event
        from("direct:FlowStep5").process(new FlowStep5Processor("FlowStep5")).to("direct:FlowStep5Completed"); // command to single event
        from("direct:FlowStep6").process(new FlowStep6Processor("FlowStep6")).to("direct:FlowStep6Completed"); // command to single event
        from("direct:FlowStep7").process(new FlowStep7Processor("FlowStep7")).to("direct:FlowTerminated"); // command to single event
        
        // routes to terminate flow
        from("direct:FlowTerminated").to("mock:bye").stop();
    }
}

// TODO split here and move each route builder class to a separate file so that compilation succeeds

public class NestedFlow2 extends RouteBuilder {
	class FlowInitiated {} 
	class FlowStep1OutEvent1 {} 
	class FlowStep1OutEvent2 {} 
	class AND_FlowStep1OutEvent1_FlowStep1OutEvent2 {} 
	class FlowStep2Completed {} 
	class FlowStep3Completed {} 
	class FlowStep4Completed {} 
	class FlowStep5Completed {} 
	class AND_FlowStep3Completed_FlowStep4Completed_FlowStep5Completed {} 
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

    class AND_FlowStep1_FlowStep2Processor implements Processor {
	    private String name;
	
        AND_FlowStep1_FlowStep2Processor(String name) {
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

    class FlowStep5Processor implements Processor {
	    private String name;
	
        FlowStep5Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class FlowStep6Processor implements Processor {
	    private String name;
	
        FlowStep6Processor(String name) {
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
        NestedFlow2 builder = new NestedFlow2();
        builder.runNestedFlow2();
    }
    
    public void runNestedFlow2() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow NestedFlow2 in FlowTest7";
		template.sendBody("direct:FlowInitiated", testMessage);
        // TODO add header to message if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:FlowInitiated", testMessage, "ChoiceCondition", "choiceValue"); 
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:FlowInitiated").to("direct:AND_FlowStep1_FlowStep2"); // event to composite command (recipient list)
        from("direct:AND_FlowStep1_FlowStep2").process(new AND_FlowStep1_FlowStep2Processor("AND_FlowStep1_FlowStep2")).recipientList(constant("direct:FlowStep1, direct:FlowStep2")).parallelProcessing(); 
        from("direct:FlowStep1OutEvent1").to("direct:FlowStep3"); // event to command
        from("direct:FlowStep1OutEvent2").to("direct:FlowStep4"); // event to command
        // note: AND_FlowStep1OutEvent1_FlowStep1OutEvent2 does not invoke any commands 
        from("direct:FlowStep2Completed").to("direct:FlowStep5"); // event to command
        // note: FlowStep3Completed does not invoke any commands 
        // note: FlowStep4Completed does not invoke any commands 
        // note: FlowStep5Completed does not invoke any commands 
        from("direct:FlowStep3Completed").to("direct:AND_FlowStep3Completed_FlowStep4Completed_FlowStep5Completed:AggregatorIn");
        from("direct:FlowStep4Completed").to("direct:AND_FlowStep3Completed_FlowStep4Completed_FlowStep5Completed:AggregatorIn");
        from("direct:FlowStep5Completed").to("direct:AND_FlowStep3Completed_FlowStep4Completed_FlowStep5Completed:AggregatorIn");
        from("direct:AND_FlowStep3Completed_FlowStep4Completed_FlowStep5Completed:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(3).to("direct:FlowStep6");
        // note: FlowTerminated does not invoke any commands 

        // routes for domain event production steps
        from("direct:FlowStep1").process(new FlowStep1Processor("FlowStep1")).recipientList(constant("direct:FlowStep1OutEvent1, direct:FlowStep1OutEvent2")).parallelProcessing(); 
        from("direct:FlowStep2").process(new FlowStep2Processor("FlowStep2")).to("direct:FlowStep2Completed"); // command to single event
        from("direct:FlowStep3").process(new FlowStep3Processor("FlowStep3")).to("direct:FlowStep3Completed"); // command to single event
        from("direct:FlowStep4").process(new FlowStep4Processor("FlowStep4")).to("direct:FlowStep4Completed"); // command to single event
        from("direct:FlowStep5").process(new FlowStep5Processor("FlowStep5")).to("direct:FlowStep5Completed"); // command to single event
        from("direct:FlowStep6").process(new FlowStep6Processor("FlowStep6")).to("direct:FlowTerminated"); // command to single event
        
        // routes to terminate flow
        from("direct:FlowTerminated").to("mock:bye").stop();
    }
}
