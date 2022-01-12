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
 
package TestCombinedStepsAndAllOptionsBrief;

// generated from orchestration flow part of test-combinedsteps-and-alloperators.mdsl
// The API description TestCombinedStepsAndAllOptionsBrief features 2 orchestration flow(s) (a.k.a. API call sequences).

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.AggregationStrategy;

public class TestFlowWithCombinedSteps extends RouteBuilder {
	class e1 {} 
	class e3 {} 
	class e2 {} 
	class AND_e2_e3 {} 
	class e4 {} 
	class e5 {} 

    class c0Processor implements Processor {
	    private String name;
	
        c0Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class c1Processor implements Processor {
	    private String name;
	
        c1Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class c2Processor implements Processor {
	    private String name;
	
        c2Processor(String name) {
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
        TestFlowWithCombinedSteps builder = new TestFlowWithCombinedSteps();
        builder.runTestFlowWithCombinedSteps();
    }
    
    public void runTestFlowWithCombinedSteps() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow TestFlowWithCombinedSteps in TestCombinedStepsAndAllOptionsBrief";
        // flow might start with command (the ones coming from miro via CML do):
        template.sendBody("direct:c0", testMessage);
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:e1").to("direct:c1"); // event to command
        // note: e3 does not invoke any commands 
        // note: e2 does not invoke any commands 
        from("direct:e2").to("direct:AND_e2_e3:AggregatorIn");
        from("direct:e3").to("direct:AND_e2_e3:AggregatorIn");
        from("direct:AND_e2_e3:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(2).to("direct:c2");
        // note: e4 does not invoke any commands 
        // note: e5 does not invoke any commands 

        // routes for domain event production steps
        from("direct:c0").choice() // command to multiple events 
        .when(simple("${header.c0EventEmissionCondition} == 'choiceOfe1'")).to("direct:e1")
        .when(simple("${header.c0EventEmissionCondition} == 'choiceOfe3'")).to("direct:e3")
        .otherwise().to("mock:bye").stop();
        from("direct:c1").process(new c1Processor("c1")).to("direct:e2"); // command to single event
        from("direct:c2").choice() // command to multiple events 
        .when(simple("${header.c2EventEmissionCondition} == 'choiceOfe4'")).to("direct:e4")
        .when(simple("${header.c2EventEmissionCondition} == 'choiceOfe5'")).to("direct:e5")
        .otherwise().to("mock:bye").stop();
        
        // routes to terminate flow
        from("direct:e5").to("mock:bye").stop();
        from("direct:e4").to("mock:bye").stop();
    }
}

// TODO split here and move each route builder class to a separate file so that compilation succeeds

public class TestFlowUsingAllOperators extends RouteBuilder {
	class e1 {} 
	class e2 {} 
	class e3 {} 
	class AND_e2_e3 {} 
	class e4 {} 
	class e5 {} 
	class AND_e3_e4 {} 

    class c1Processor implements Processor {
	    private String name;
	
        c1Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class c2Processor implements Processor {
	    private String name;
	
        c2Processor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class c3Processor implements Processor {
	    private String name;
	
        c3Processor(String name) {
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
        TestFlowUsingAllOperators builder = new TestFlowUsingAllOperators();
        builder.runTestFlowUsingAllOperators();
    }
    
    public void runTestFlowUsingAllOperators() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow TestFlowUsingAllOperators in TestCombinedStepsAndAllOptionsBrief";
		template.sendBody("direct:e1", testMessage);
        // TODO add header to message if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:e1", testMessage, "ChoiceCondition", "choiceValue"); 
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:e1").choice() // event to multiple commands 
        .when(simple("${header.e1CommandInvocationCondition} == 'choiceOfc1'")).to("direct:c1") 
        .when(simple("${header.e1CommandInvocationCondition} == 'choiceOfc2'")).to("direct:c2")
        .otherwise().to("mock:bye").stop();
        // note: e2 does not invoke any commands 
        // note: e3 does not invoke any commands 
        // note: AND_e2_e3 does not invoke any commands 
        // note: e4 does not invoke any commands 
        // note: e5 does not invoke any commands 
        from("direct:e3").to("direct:AND_e3_e4:AggregatorIn");
        from("direct:e4").to("direct:AND_e3_e4:AggregatorIn");
        from("direct:AND_e3_e4:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(2).to("direct:c3");

        // routes for domain event production steps
        from("direct:c1").process(new c1Processor("c1")).recipientList(constant("direct:e2, direct:e3")).parallelProcessing(); 
        from("direct:c2").choice() // command to multiple events 
        .when(simple("${header.c2EventEmissionCondition} == 'choiceOfe4'")).to("direct:e4")
        .when(simple("${header.c2EventEmissionCondition} == 'choiceOfe5'")).to("direct:e5")
        .otherwise().to("mock:bye").stop();
        from("direct:c3").choice() // command to multiple events 
        .when(simple("${header.c3EventEmissionCondition} == 'choiceOfe4'")).to("direct:e4")
        .when(simple("${header.c3EventEmissionCondition} == 'choiceOfe5'")).to("direct:e5")
        .otherwise().to("mock:bye").stop();
        
        // routes to terminate flow
        from("direct:e5").to("mock:bye").stop();
        from("direct:e2").to("mock:bye").stop();
    }
}
