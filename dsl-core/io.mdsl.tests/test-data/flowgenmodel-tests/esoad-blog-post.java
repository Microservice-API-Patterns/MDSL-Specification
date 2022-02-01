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
 
package EventStormingBoardContextAPI;

// generated from orchestration flow part of esoad-blog-post.mdsl
// The API description EventStormingBoardContextAPI features 3 orchestration flow(s) (a.k.a. API call sequences).

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.AggregationStrategy;

public class Write_Paper extends RouteBuilder {
	class Research_Result_Available {} 
	class Paper_Submitted_for_Review {} 
	class Paper_Will_Be_Revised {} 
	class Acceptance_Decision_Made {} 
	class Feedback_Sent {} 
	class Proceedings_Available {} 
	class Paper_Available_Online {} 

    class Write_PaperCommandProcessor implements Processor {
	    private String name;
	
        Write_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_PaperCommandProcessor implements Processor {
	    private String name;
	
        Review_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_FeedbackCommandProcessor implements Processor {
	    private String name;
	
        Review_FeedbackCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Accept_PaperCommandProcessor implements Processor {
	    private String name;
	
        Accept_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Reject_PaperCommandProcessor implements Processor {
	    private String name;
	
        Reject_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Publish_Authors_CopyCommandProcessor implements Processor {
	    private String name;
	
        Publish_Authors_CopyCommandProcessor(String name) {
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
        Write_Paper builder = new Write_Paper();
        builder.runWrite_Paper();
    }
    
    public void runWrite_Paper() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow Write_Paper in EventStormingBoardContextAPI";
		template.sendBody("direct:Research_Result_Available", testMessage);
        // TODO add header to message if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:Research_Result_Available", testMessage, "ChoiceCondition", "choiceValue"); 
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:Research_Result_Available").to("direct:Write_PaperCommand"); // event to command
        from("direct:Paper_Submitted_for_Review").to("direct:Review_PaperCommand"); // event to command
        from("direct:Paper_Will_Be_Revised").to("direct:Write_PaperCommand"); // event to command
        from("direct:Acceptance_Decision_Made").choice() // event to multiple commands 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfAccept_PaperCommand'")).to("direct:Accept_PaperCommand") 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfReject_PaperCommand'")).to("direct:Reject_PaperCommand")
        .otherwise().to("mock:bye").stop();
        from("direct:Feedback_Sent").to("direct:Review_FeedbackCommand"); // event to command
        from("direct:Proceedings_Available").to("direct:Publish_Authors_CopyCommand"); // event to command
        // note: Paper_Available_Online does not invoke any commands 

        // routes for domain event production steps
        from("direct:Write_PaperCommand").process(new Write_PaperCommandProcessor("Write_PaperCommand")).to("direct:Paper_Submitted_for_Review"); // command to single event
        from("direct:Review_PaperCommand").process(new Review_PaperCommandProcessor("Review_PaperCommand")).to("direct:Acceptance_Decision_Made"); // command to single event
        from("direct:Review_FeedbackCommand").process(new Review_FeedbackCommandProcessor("Review_FeedbackCommand")).to("direct:Paper_Will_Be_Revised"); // command to single event
        from("direct:Accept_PaperCommand").process(new Accept_PaperCommandProcessor("Accept_PaperCommand")).to("direct:Proceedings_Available"); // command to single event
        from("direct:Reject_PaperCommand").process(new Reject_PaperCommandProcessor("Reject_PaperCommand")).to("direct:Feedback_Sent"); // command to single event
        from("direct:Publish_Authors_CopyCommand").process(new Publish_Authors_CopyCommandProcessor("Publish_Authors_CopyCommand")).to("direct:Paper_Available_Online"); // command to single event
        
        // routes to terminate flow
        from("direct:Paper_Available_Online").to("mock:bye").stop();
    }
}

// TODO split here and move each route builder class to a separate file so that compilation succeeds

public class Write_Paper_WithInitCommand extends RouteBuilder {
	class Research_Result_Available {} 
	class Paper_Submitted_for_Review {} 
	class Paper_Will_Be_Revised {} 
	class Acceptance_Decision_Made {} 
	class Feedback_Sent {} 
	class Proceedings_Available {} 
	class Paper_Available_Online {} 

    class Conduct_Research_CommandProcessor implements Processor {
	    private String name;
	
        Conduct_Research_CommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Write_PaperCommandProcessor implements Processor {
	    private String name;
	
        Write_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_PaperCommandProcessor implements Processor {
	    private String name;
	
        Review_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_FeedbackCommandProcessor implements Processor {
	    private String name;
	
        Review_FeedbackCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Accept_PaperCommandProcessor implements Processor {
	    private String name;
	
        Accept_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Reject_PaperCommandProcessor implements Processor {
	    private String name;
	
        Reject_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Publish_Authors_CopyCommandProcessor implements Processor {
	    private String name;
	
        Publish_Authors_CopyCommandProcessor(String name) {
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
        Write_Paper_WithInitCommand builder = new Write_Paper_WithInitCommand();
        builder.runWrite_Paper_WithInitCommand();
    }
    
    public void runWrite_Paper_WithInitCommand() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow Write_Paper_WithInitCommand in EventStormingBoardContextAPI";
        template.sendBody("direct:Conduct_Research_Command", testMessage);
        // TODO add header to testMessage if flow contains a choice, example (replace "choice-nn" with names from flow):
        // template.sendBodyAndHeader("direct:Conduct_Research_Command", testMessage, "ChoiceCondition", "choiceValue");
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:Research_Result_Available").to("direct:Write_PaperCommand"); // event to command
        from("direct:Paper_Submitted_for_Review").to("direct:Review_PaperCommand"); // event to command
        from("direct:Paper_Will_Be_Revised").to("direct:Write_PaperCommand"); // event to command
        from("direct:Acceptance_Decision_Made").choice() // event to multiple commands 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfAccept_PaperCommand'")).to("direct:Accept_PaperCommand") 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfReject_PaperCommand'")).to("direct:Reject_PaperCommand")
        .otherwise().to("mock:bye").stop();
        from("direct:Feedback_Sent").to("direct:Review_FeedbackCommand"); // event to command
        from("direct:Proceedings_Available").to("direct:Publish_Authors_CopyCommand"); // event to command
        // note: Paper_Available_Online does not invoke any commands 

        // routes for domain event production steps
        from("direct:Conduct_Research_Command").process(new Conduct_Research_CommandProcessor("Conduct_Research_Command")).to("direct:Research_Result_Available"); // command to single event
        from("direct:Write_PaperCommand").process(new Write_PaperCommandProcessor("Write_PaperCommand")).to("direct:Paper_Submitted_for_Review"); // command to single event
        from("direct:Review_PaperCommand").process(new Review_PaperCommandProcessor("Review_PaperCommand")).to("direct:Acceptance_Decision_Made"); // command to single event
        from("direct:Review_FeedbackCommand").process(new Review_FeedbackCommandProcessor("Review_FeedbackCommand")).to("direct:Paper_Will_Be_Revised"); // command to single event
        from("direct:Accept_PaperCommand").process(new Accept_PaperCommandProcessor("Accept_PaperCommand")).to("direct:Proceedings_Available"); // command to single event
        from("direct:Reject_PaperCommand").process(new Reject_PaperCommandProcessor("Reject_PaperCommand")).to("direct:Feedback_Sent"); // command to single event
        from("direct:Publish_Authors_CopyCommand").process(new Publish_Authors_CopyCommandProcessor("Publish_Authors_CopyCommand")).to("direct:Paper_Available_Online"); // command to single event
        
        // routes to terminate flow
        from("direct:Paper_Available_Online").to("mock:bye").stop();
    }
}
public class Write_Paper_NoInitEventOrCommand extends RouteBuilder {
	class Paper_Submitted_for_Review {} 
	class Paper_Will_Be_Revised {} 
	class Acceptance_Decision_Made {} 
	class Feedback_Sent {} 
	class Proceedings_Available {} 
	class Paper_Available_Online {} 

    class Write_PaperCommandProcessor implements Processor {
	    private String name;
	
        Write_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_PaperCommandProcessor implements Processor {
	    private String name;
	
        Review_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Review_FeedbackCommandProcessor implements Processor {
	    private String name;
	
        Review_FeedbackCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Accept_PaperCommandProcessor implements Processor {
	    private String name;
	
        Accept_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Reject_PaperCommandProcessor implements Processor {
	    private String name;
	
        Reject_PaperCommandProcessor(String name) {
    	    this.name=name;
        }

        public void process(Exchange exchange) throws Exception {
    	    String message = exchange.getIn().getBody().toString();
    	    System.out.println("Command processor " + this.getClass().getSimpleName() + " activated, processing message: " + message);
            exchange.getIn().setBody(message + ", processed by " + this.getClass().getSimpleName()); 
        }
    }

    class Publish_Authors_CopyCommandProcessor implements Processor {
	    private String name;
	
        Publish_Authors_CopyCommandProcessor(String name) {
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
        Write_Paper_NoInitEventOrCommand builder = new Write_Paper_NoInitEventOrCommand();
        builder.runWrite_Paper_NoInitEventOrCommand();
    }
    
    public void runWrite_Paper_NoInitEventOrCommand() throws Exception {
        // create CamelContext
        DefaultCamelContext camelContext = new DefaultCamelContext();

        /* bindBeans(camelContext.getRegistry()); */ 
        camelContext.addRoutes(this);
        camelContext.setTracing(true);
        camelContext.start();

        // create a producer to send all initial events to the process flow
        ProducerTemplate template = camelContext.createProducerTemplate();
        String testMessage = "Test message for flow Write_Paper_NoInitEventOrCommand in EventStormingBoardContextAPI";
        camelContext.stop();
        camelContext.close();
    }

    @Override
    public void configure() {
        // routes for command invocation steps
        from("direct:Paper_Submitted_for_Review").to("direct:Review_PaperCommand"); // event to command
        from("direct:Paper_Will_Be_Revised").to("direct:Write_PaperCommand"); // event to command
        from("direct:Acceptance_Decision_Made").choice() // event to multiple commands 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfAccept_PaperCommand'")).to("direct:Accept_PaperCommand") 
        .when(simple("${header.Acceptance_Decision_MadeCommandInvocationCondition} == 'choiceOfReject_PaperCommand'")).to("direct:Reject_PaperCommand")
        .otherwise().to("mock:bye").stop();
        from("direct:Feedback_Sent").to("direct:Review_FeedbackCommand"); // event to command
        from("direct:Proceedings_Available").to("direct:Publish_Authors_CopyCommand"); // event to command
        // note: Paper_Available_Online does not invoke any commands 

        // routes for domain event production steps
        from("direct:Write_PaperCommand").process(new Write_PaperCommandProcessor("Write_PaperCommand")).to("direct:Paper_Submitted_for_Review"); // command to single event
        from("direct:Review_PaperCommand").process(new Review_PaperCommandProcessor("Review_PaperCommand")).to("direct:Acceptance_Decision_Made"); // command to single event
        from("direct:Review_FeedbackCommand").process(new Review_FeedbackCommandProcessor("Review_FeedbackCommand")).to("direct:Paper_Will_Be_Revised"); // command to single event
        from("direct:Accept_PaperCommand").process(new Accept_PaperCommandProcessor("Accept_PaperCommand")).to("direct:Proceedings_Available"); // command to single event
        from("direct:Reject_PaperCommand").process(new Reject_PaperCommandProcessor("Reject_PaperCommand")).to("direct:Feedback_Sent"); // command to single event
        from("direct:Publish_Authors_CopyCommand").process(new Publish_Authors_CopyCommandProcessor("Publish_Authors_CopyCommand")).to("direct:Paper_Available_Online"); // command to single event
        
        // routes to terminate flow
        from("direct:Paper_Available_Online").to("mock:bye").stop();
    }
}
