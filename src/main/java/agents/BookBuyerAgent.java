package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class BookBuyerAgent extends Agent{

    private String targetBook;
    private AID[] sellerAgents;


    protected void setup(){
        System.out.println("Hello from " + getAID().getName() + "!");

        Object[] args = getArguments();

        if(args !=null && args.length > 0){
            targetBook = (String) args[0];
            System.out.println("I am looking for the book " + targetBook);

            addBehaviour(new TickerBehaviour(this, 5000) { //runs every 5 seconds
                @Override
                protected void onTick() {
                    System.out.println("Searching for book sellers...");
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book-seller");
                    template.addServices(sd);
                    
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents: ");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; i++) {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
            
                        if (sellerAgents.length > 0) {
                            // If we have found seller agents, stop the TickerBehaviour and start RequestPerformer
                            System.out.println("Sellers found. Starting Request Performer.");
                            myAgent.addBehaviour(new RequestPerformer());
                            stop();  // Stop the TickerBehaviour
                        }
            
                    } catch (FIPAException e) {
                        System.err.println("Error searching for seller agents: " + e.getMessage());
                    }
                }               
            });
        }
        else{
            System.out.println("No book specified in the agent's argument");
            doDelete();
        }
    }

    protected void takeDown(){
        System.out.println("Goodbye from " + getAID().getName() + "!");
    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller;
        private int bestPrice;
        private int repliesCount = 0;
        private MessageTemplate msgTemplate;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    // Send CFP to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID sellerAgent : sellerAgents) {
                        cfp.addReceiver(sellerAgent);
                    }
                    cfp.setContent(targetBook);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    msgTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
                    );
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender(); // Set the best seller
                            }
                        }
                        step = 2;
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // If a valid bestSeller exists, send ACCEPT_PROPOSAL
                    if (bestSeller == null) {
                        System.out.println("No seller was found or no acceptable price.");
                        myAgent.doDelete();
                    } else {
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(bestSeller);
                        order.setContent(targetBook);
                        order.setConversationId("book-trade");
                        order.setReplyWith("order" + System.currentTimeMillis());
                        myAgent.send(order);
                        msgTemplate = MessageTemplate.and(
                                MessageTemplate.MatchConversationId("book-trade"),
                                MessageTemplate.MatchInReplyTo(order.getReplyWith())
                        );
                        step = 3;
                    }
                    break;
                case 3:
                    // Receive the purchase order confirmation
                    reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful
                            System.out.println(targetBook + " successfully purchased from agent " + reply.getSender().getName());
                            System.out.println("Price = " + bestPrice);
                            myAgent.doDelete();
                        } else {
                            System.out.println("Attempt failed: requested book already sold.");
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }
        

        @Override
        public boolean done() { //complete agent
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }//end RequestPerformer
}
