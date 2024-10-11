package agents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent {

    private String targetBook;
    private AID bestSeller;
    private int bestPrice;
    private int repliesCnt = 0;
    private AID[] sellerAgents;
    private MessageTemplate mt;
    private int step = 0;

    protected void setup() {
        System.out.println("Hello from " + getAID().getName() + "!");

        // Get the book to buy from arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBook = (String) args[0];
            System.out.println("I am looking for the book " + targetBook);

            // Add a TickerBehaviour to search for sellers every 5 seconds
            addBehaviour(new TickerBehaviour(this, 5000) {
                @Override
                protected void onTick() {
                    System.out.println("Searching for book sellers...");

                    // Update list of seller agents
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
                            // Sellers found, stop searching and start RequestPerformer
                            myAgent.addBehaviour(new RequestPerformer());
                            stop(); // Stop the ticker behaviour
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            });
        } else {
            System.out.println("No target book specified");
            doDelete();
        }
    }

    private class RequestPerformer extends jade.core.behaviours.Behaviour {

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

                    // Prepare template to get replies
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;

                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;

                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBook);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);

                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;

                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
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

        public boolean done() {
            return (step == 2 && bestSeller == null) || step == 4;
        }
    }
}
