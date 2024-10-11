package agents;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {

    // Catalogue of books: title -> price
    private Hashtable<String, Integer> catalogue;

    protected void setup() {
        catalogue = new Hashtable<>();

        // Get the book details from arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String bookTitle = (String) args[0];
            int price = Integer.parseInt((String) args[1]);
            int quantity = Integer.parseInt((String) args[2]);
            catalogue.put(bookTitle, price);
            System.out.println("Added to catalog : " + bookTitle + " | Price " + price + " | Quantity " + quantity);
        }

        // Register the book-selling service in the yellow pages (DF)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("[" + getAID().getName() + "] registered successfully with DF");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add behaviour to listen for incoming requests (CFP) from buyers
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    ACLMessage reply = msg.createReply();
                    switch (msg.getPerformative()) {
                        case ACLMessage.CFP:
                            Integer price = catalogue.get(content);
                            if (price != null) {
                                // Propose price
                                reply.setPerformative(ACLMessage.PROPOSE);
                                reply.setContent(String.valueOf(price));
                                System.out.println("[" + getAID().getName() + "] Proposing price " + price + " for " + content);
                            } else {
                                // Book not available
                                reply.setPerformative(ACLMessage.REFUSE);
                                reply.setContent("not-available");
                            }
                            break;
                        case ACLMessage.ACCEPT_PROPOSAL:
                            // Purchase accepted, book sold
                            if (catalogue.containsKey(content)) {
                                reply.setPerformative(ACLMessage.INFORM);
                                System.out.println("[" + getAID().getName() + "] Book sold: " + content);
                            } else {
                                reply.setPerformative(ACLMessage.FAILURE);
                                System.out.println("[" + getAID().getName() + "] Failed to sell " + content + ". Out of stock.");
                            }
                            break;
                        default:
                            // Ignore other message types
                            block();
                            return;
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
            }
        });
    }

    protected void takeDown() {
        // Deregister from the yellow pages (DF)
        try {
            DFService.deregister(this);
            System.out.println("[" + getAID().getName() + "] successfully deregistered from the DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Seller agent " + getAID().getName() + " terminating.");
    }
}
