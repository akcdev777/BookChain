package test;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.AchieveREResponder;
import jnr.ffi.annotations.In;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HouseholdAgent extends Agent {
    private List<Integer> dailyAllocation = Collections.synchronizedList(new ArrayList<>()); // Array to store the 4 values
    private final List<Integer> preferredTimeSlots = Collections.synchronizedList(new ArrayList<>());
    private List<Integer> requestSlots = Collections.synchronizedList(new ArrayList<>());
    private final Queue<Integer> requestQueue = new ConcurrentLinkedQueue<>();




    private AID brokerAID;
    private static final AddressGenerator addressGenerator = new AddressGenerator();
    private final String PADDRESS = AddressGenerator.generateRandomAddress();



    protected void setup() {
        System.out.println("HouseholdAgent " + getAID().getName() + " is ready.");

        // Step 1: Query DF for BrokerAgent (you said this is already implemented)
        brokerAID = findBrokerAID();
        if (brokerAID == null) {
            System.out.println("Broker not found. Terminating.");
            doDelete();
            return;
        }

        // Step 2: Send spawn message to Broker using FIPA-Request
        addBehaviour(new SendSpawnedMessage());
        addBehaviour(new Dispatcher());


    }

    private class Dispatcher extends CyclicBehaviour{
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                System.out.println("Household received message: performative=" + ACLMessage.getPerformative(msg.getPerformative()) +
                        ", protocol=" + msg.getProtocol() +
                        ", content=" + msg.getContent() +
                        ", sender=" + msg.getSender().getName() +
                        ", conversation-id=" + msg.getConversationId());

                // Match FIPA-Request with specific content
                if (msg.getPerformative() == ACLMessage.REQUEST &&
                        "fipa-request".equals(msg.getProtocol())) {
                    if (msg.getContent() != null && msg.getContent().equals("provide-exchange-slot")) {
                        System.out.println("Exchange slot request detected, adding ExchangeSlotResponder.");
                        myAgent.putBack(msg); // Put back for responder
                        myAgent.addBehaviour(new ExchangeSlotResponder(myAgent, msg));
                    } else {
                        System.out.println("Unhandled request content: " + msg.getContent());
                    }
                } else {
                    System.out.println("Message not handled: " + msg.getContent());
                }
            } else {
                block();
            }
        }
    }

    private class SendSpawnedMessage extends OneShotBehaviour {
        public void action() {
            // Step 4: Send spawned message to LoggerAgent
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(brokerAID);
            request.setProtocol("fipa-request");
            request.setContent("spawn:" + PADDRESS);
            request.setReplyByDate(new java.util.Date(System.currentTimeMillis() + 10000)); // 10 seconds

            addBehaviour(new InitiateSpawnRequest(myAgent, request));
        }
    }

    private class InitiateSpawnRequest extends AchieveREInitiator{
        public InitiateSpawnRequest(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected void handleInform(ACLMessage inform) {
            // Step 3: Parse the initial-allocation reply
            String content = inform.getContent();
            if (content.startsWith("initial-allocation:")) {
                String values = content.substring("initial-allocation:".length());
                String[] splitValues = values.split(",");
                if (splitValues.length == 4) {
                    for (int i = 0; i < 4; i++) {
                        dailyAllocation.add(Integer.parseInt(splitValues[i].trim()));
                    }
                    System.out.println(myAgent.getLocalName() + " : Received allocation: " + dailyAllocation);
                } else {
                    System.out.println("Invalid allocation format: " + content);
                }
            }

            // Step 5: Generate preferred time slots once initial allocation is received
            generatePreferredTimeSlots();

            // Step 6: Update request slots
            updateRequestSlots();

            // Update request queue
            setRequestQueue();
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Broker refused spawn: " + refuse.getContent());
        }

        protected void handleFailure(ACLMessage failure) {
            System.out.println("Broker failed to process spawn: " + failure.getContent());
        }

        protected void handleAgree(ACLMessage agree) {
            System.out.println("Broker agreed to spawn: " + agree.getContent());
        }
    }

    public class ExchangeSlotResponder extends AchieveREResponder {
        private ACLMessage request;

        public ExchangeSlotResponder(Agent a, ACLMessage request) {
            super(a, MessageTemplate.and(
                    MessageTemplate.MatchConversationId(request.getConversationId()),
                    MessageTemplate.MatchProtocol("fipa-request")));
            this.request = request;
            System.out.println("ExchangeSlotResponder initialized for conversation ID: " + request.getConversationId());
        }

        protected ACLMessage prepareResponse(ACLMessage msg) {
            try {
                System.out.println("prepareResponse called with msg: " + (msg == null ? "null" : msg.getContent()) +
                        ", stored request: " + (request == null ? "null" : request.getContent()));
                if (request == null) {
                    System.out.println("Error: No request stored!");
                    return null;
                }

                String content = request.getContent();
                System.out.println("Processing request content: " + content);
                if (content != null && content.equals("provide-exchange-slot")) {
                    if (requestQueue.isEmpty()) {
                        ACLMessage refuse = request.createReply();
                        refuse.setPerformative(ACLMessage.REFUSE);
                        refuse.setContent("No available slots");
                        refuse.setInReplyTo(request.getReplyWith());
                        System.out.println("Sending REFUSE due to no available slots");
                        return refuse;
                    } else {
                        ACLMessage agree = request.createReply();
                        agree.setPerformative(ACLMessage.AGREE);
                        agree.setInReplyTo(request.getReplyWith());
                        agree.setContent("exchange-slot");
                        System.out.println("Sending AGREE to " + request.getSender().getName());
                        return agree;
                    }
                } else {
                    ACLMessage refuse = request.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    refuse.setContent("Invalid request");
                    refuse.setInReplyTo(request.getReplyWith());
                    System.out.println("Sending REFUSE due to invalid content");
                    return refuse;
                }
            } catch (Exception e) {
                System.out.println("Exception in prepareResponse: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {
            try {
                System.out.println("prepareResultNotification called with msg: " + (msg == null ? "null" : msg.getContent()) +
                        ", response: " + (response == null ? "null" : response.getPerformative()));
                if (request == null) {
                    System.out.println("Error: No request stored!");
                    return null;
                }

                String content = request.getContent();
                System.out.println("Preparing result for exchange slot request: " + content);
                requestSlots.clear();

                Integer requestSlot = requestQueue.poll();
                if (requestSlot != null) {
                    String responseContent = "exchange-slot:" + requestSlot + ",offer-slots:" + getOfferedSlots();
                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setInReplyTo(request.getReplyWith());
                    inform.setContent(responseContent);
                    System.out.println("Sending INFORM with content: " + responseContent);
                    return inform;
                } else {
                    ACLMessage refuse = request.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    refuse.setContent("No available slots");
                    refuse.setInReplyTo(request.getReplyWith());
                    System.out.println("Sending REFUSE due to no available slots");
                    return refuse;
                }
            } catch (Exception e) {
                System.out.println("Exception in prepareResultNotification: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        private List<Integer> getOfferedSlots() {
            List<Integer> offeredSlots = new ArrayList<>(dailyAllocation);
            offeredSlots.removeAll(preferredTimeSlots);
            return offeredSlots;
        }
    }
    private List<Integer> getOfferedSlots(){
        List<Integer> offeredSlots = new ArrayList<>(dailyAllocation);
        offeredSlots.removeAll(preferredTimeSlots);
        return offeredSlots;
    }

    private void setRequestQueue(){
        requestQueue.addAll(requestSlots);
    }


    // Placeholder for your existing DF query logic
    private AID findBrokerAID() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("broker-service"); // Adjust based on your DF registration
            template.addServices(sd);
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generatePreferredTimeSlots() {
        Random random = new Random();
        preferredTimeSlots.clear();
        Set<Integer> uniqueSlots = new HashSet<>();
        while (uniqueSlots.size() < 4) {
            uniqueSlots.add(random.nextInt(24));
        }
        preferredTimeSlots.addAll(uniqueSlots);
        System.out.println(getAID().getLocalName() + ": Generated preferred time slots = " + preferredTimeSlots);
    }

    private void updateRequestSlots(){
        requestSlots.clear();
        requestSlots.addAll(preferredTimeSlots);
        requestSlots.removeAll(dailyAllocation);

        System.out.println(getAID().getLocalName() + ": Updated request slots = " + requestSlots);
    }

    private List<Integer> getPreferredTimeSlots() {
        return preferredTimeSlots;
    }

    // Getter for testing purposes
    public List<Integer> getDailyAllocation() {
        return dailyAllocation;
    }


}