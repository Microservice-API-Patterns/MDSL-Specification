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
 
package FlowTest2;

// generated from orchestration flow part of flowtest2-parallelsplitwithsynchronization.mdsl
// The API description FlowTest2 features 2 orchestration flow(s) (a.k.a. API call sequences).

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.AggregationStrategy;

public class ParallelSplit extends RouteBuilder {
	class FlowInitiated {} 
	class FlowStep1Completed {} 
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

    class AND_FlowStep2_FlowStep3Processor implements Processor {
	    private String name;
	
        AND_FlowStep2_FlowStep3Processor(String name) {
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
        ParallelSplit builder = new ParallelSplit();
        builder.runParallelSplit();
    }
    
    public void runParallelSplit() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow ParallelSplit in FlowTest2";
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
        from("direct:FlowStep1Completed").to("direct:AND_FlowStep2_FlowStep3"); // event to composite command (recipient list)
        from("direct:AND_FlowStep2_FlowStep3").process(new AND_FlowStep2_FlowStep3Processor("AND_FlowStep2_FlowStep3")).recipientList(constant("direct:FlowStep2, direct:FlowStep3")).parallelProcessing(); 
        // note: FlowTerminated does not invoke any commands 

        // routes for domain event production steps
        from("direct:FlowStep1").process(new FlowStep1Processor("FlowStep1")).to("direct:FlowStep1Completed"); // command to single event
        from("direct:FlowStep2").process(new FlowStep2Processor("FlowStep2")).to("direct:FlowTerminated"); // command to single event
        from("direct:FlowStep3").process(new FlowStep3Processor("FlowStep3")).to("direct:FlowTerminated"); // command to single event
        
        // routes to terminate flow
        from("direct:FlowTerminated").to("mock:bye").stop();
    }
}

// TODO split here and move each route builder class to a separate file so that compilation succeeds

public class ParallelSplitWithSynchronization extends RouteBuilder {
	class FlowInitiated {} 
	class FlowStep1Completed {} 
	class FlowStep2Completed {} 
	class FlowStep3Completed {} 
	class AND_FlowStep2Completed_FlowStep3Completed {} 
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

    class AND_FlowStep2_FlowStep3Processor implements Processor {
	    private String name;
	
        AND_FlowStep2_FlowStep3Processor(String name) {
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
        ParallelSplitWithSynchronization builder = new ParallelSplitWithSynchronization();
        builder.runParallelSplitWithSynchronization();
    }
    
    public void runParallelSplitWithSynchronization() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow ParallelSplitWithSynchronization in FlowTest2";
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
        from("direct:FlowStep1Completed").to("direct:AND_FlowStep2_FlowStep3"); // event to composite command (recipient list)
        from("direct:AND_FlowStep2_FlowStep3").process(new AND_FlowStep2_FlowStep3Processor("AND_FlowStep2_FlowStep3")).recipientList(constant("direct:FlowStep2, direct:FlowStep3")).parallelProcessing(); 
        // note: FlowStep2Completed does not invoke any commands 
        // note: FlowStep3Completed does not invoke any commands 
        from("direct:FlowStep2Completed").to("direct:AND_FlowStep2Completed_FlowStep3Completed:AggregatorIn");
        from("direct:FlowStep3Completed").to("direct:AND_FlowStep2Completed_FlowStep3Completed:AggregatorIn");
        from("direct:AND_FlowStep2Completed_FlowStep3Completed:AggregatorIn").aggregate(new JoinAggregatorStrategy()).constant(true).completionSize(2).to("direct:FlowStep4");
        // note: FlowTerminated does not invoke any commands 

        // routes for domain event production steps
        from("direct:FlowStep1").process(new FlowStep1Processor("FlowStep1")).to("direct:FlowStep1Completed"); // command to single event
        from("direct:FlowStep2").process(new FlowStep2Processor("FlowStep2")).to("direct:FlowStep2Completed"); // command to single event
        from("direct:FlowStep3").process(new FlowStep3Processor("FlowStep3")).to("direct:FlowStep3Completed"); // command to single event
        from("direct:FlowStep4").process(new FlowStep4Processor("FlowStep4")).to("direct:FlowTerminated"); // command to single event
        
        // routes to terminate flow
        from("direct:FlowTerminated").to("mock:bye").stop();
    }
}
