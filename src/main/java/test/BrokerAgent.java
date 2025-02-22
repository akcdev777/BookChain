package test;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;

import java.util.*;

public class BrokerAgent extends Agent {
    private static final int MAX_INITIAL_ALLOCATION = 4;
    private static final int UNIQUE_TIME_SLOTS = 24;
    private int expectedHouseholds = 1;
    private Map<String, AID> householdRegistry = new HashMap<>(); // Store publicAddress -> AID
    private Map<AID, List<Integer>> initialAllocationsMap = new HashMap<>(); // Store publicAddress -> AID
    private Set<AID> selectedHouseholds = new HashSet<>();
    private int totalHouseholds = 1;
    private final Random random = new Random();
    private boolean pause = false;
    private ExchangeOffer currentExchangeOffer;


    protected void setup() {
        System.out.println("BrokerAgent " + getAID().getName() + " is ready.");

        // Register with DF
        registerWithDF();

        // Add CyclicBehaviour to process all incoming messages
        addBehaviour(new Dispatcher());

        // Add OneShotBehaviour to initiate exchange slot requests
        addBehaviour(new ExchangeTicker(this, 7000));


    }

    private class Dispatcher extends CyclicBehaviour{
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                System.out.println("Broker received message: performative=" + ACLMessage.getPerformative(msg.getPerformative()) +
                        ", protocol=" + msg.getProtocol() +
                        ", content=" + msg.getContent() +
                        ", sender=" + msg.getSender().getName() +
                        ", conversation-id=" + msg.getConversationId());

                if (msg.getPerformative() == ACLMessage.REQUEST &&
                        "fipa-request".equals(msg.getProtocol()) &&
                        msg.getContent() != null && msg.getContent().startsWith("spawn:")) {
                    System.out.println("Spawn message detected, adding SpawnResponder.");
                    // Pass the message to SpawnResponder and put it back in the queue
                    myAgent.putBack(msg);
                    myAgent.addBehaviour(new SpawnResponder(myAgent, msg));
                } else {
                    System.out.println("Message not handled: " + msg.getContent());
                    myAgent.putBack(msg); // Put unhandled messages back
                }
            } else {
                block();
            }
        }
    }

    private int getHouseholdCount() {
        return householdRegistry.size();
    }

    // Dedicated behaviour for handling spawn requests
    private class SpawnResponder extends AchieveREResponder {
        private ACLMessage request;

        public SpawnResponder(Agent a, ACLMessage request) {
            // Use a template matching both conversation ID and protocol
            super(a, MessageTemplate.and(
                    MessageTemplate.MatchConversationId(request.getConversationId()),
                    MessageTemplate.MatchProtocol("fipa-request")));
            this.request = request; // Store the request explicitly
            System.out.println("SpawnResponder initialized for conversation ID: " + request.getConversationId());
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
                if (content != null && content.startsWith("spawn:")) {
                    ACLMessage agree = request.createReply();
                    agree.setPerformative(ACLMessage.AGREE);
                    agree.setInReplyTo(request.getReplyWith()); // Link back to the original request
                    System.out.println("Sending AGREE to " + request.getSender().getName() +
                            ", in-reply-to: " + request.getReplyWith());
                    return agree;
                } else {
                    ACLMessage refuse = request.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    refuse.setContent("Invalid spawn request");
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
                String publicAddress = content.substring("spawn:".length());
                AID householdAID = request.getSender();

                householdRegistry.put(publicAddress, householdAID);
                System.out.println("Registered household: " + publicAddress + " with AID: " + householdAID.getName());

                List<Integer> initialSlots = generateRandomSlots();
                initialAllocationsMap.put(msg.getSender(), initialSlots);

                String allocationStr = String.format("initial-allocation:%d,%d,%d,%d",
                        initialSlots.get(0), initialSlots.get(1), initialSlots.get(2), initialSlots.get(3));

                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setInReplyTo(request.getReplyWith());
                inform.setContent(allocationStr);
                System.out.println("Sending INFORM with allocation: " + allocationStr +
                        ", in-reply-to: " + request.getReplyWith());
                return inform;
            } catch (Exception e) {
                System.out.println("Exception in prepareResultNotification: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        private List<Integer> generateRandomSlots() {
            Set<Integer> slots = new HashSet<>();
            while (slots.size() < MAX_INITIAL_ALLOCATION) {
                slots.add(random.nextInt(UNIQUE_TIME_SLOTS));
            }
            return new ArrayList<>(slots);
        }
    }


    private class InitiateExchange extends OneShotBehaviour {
        public void action() {
            if (!householdRegistry.isEmpty()) {
                pause();
                AID householdAID = null;
                for (AID aid : householdRegistry.values()) {
                    if (!selectedHouseholds.contains(aid)) {
                        householdAID = aid;
                        break;
                    }
                }if (householdAID != null) {
                    System.out.println("Triggering ExchangeSlotInitiator for " + householdAID.getName());
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.addReceiver(householdAID);
                    request.setProtocol("fipa-request");
                    request.setContent("provide-exchange-slot");
                    request.setConversationId("exchange-slot-" + System.currentTimeMillis() + "_" + householdAID.getLocalName());
                    request.setReplyWith("R" + System.currentTimeMillis() + "_0");
                    addBehaviour(new ExchangeSlotInitiator(myAgent, request));
                } else {
                    System.out.println("All households have been given the exchange turn.");
                }
            } else {
                System.out.println("No households registered yet.");
            }
        }
    }
    private class ExchangeSlotInitiator extends AchieveREInitiator {
        public ExchangeSlotInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
            System.out.println("ExchangeSlotInitiator initialized for conversation ID: " + msg.getConversationId());
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("Received INFORM from household: " + inform.getContent());
            if (inform.getContent() != null && inform.getContent().startsWith("exchange-slot:")) {
                String content = inform.getContent().substring("exchange-slot:".length());
                String[] parts = content.split(",offer-slots:");
                if (parts.length == 2) {
                    int exchangeSlot = Integer.parseInt(parts[0].trim());
                    String offerSlotsStr = parts[1].trim();
                    offerSlotsStr = offerSlotsStr.substring(1, offerSlotsStr.length() - 1); // Remove the square brackets
                    List<Integer> offerSlots = new ArrayList<>();
                    for (String slot : offerSlotsStr.split(",")) {
                        offerSlots.add(Integer.parseInt(slot.trim()));
                    }
                    System.out.println("Parsed exchange slot: " + exchangeSlot);
                    System.out.println("Parsed offer slots: " + offerSlots);

                    // logic to handle the exchange slot and offer slots here
                    currentExchangeOffer = new ExchangeOffer(inform.getSender(), exchangeSlot, offerSlots);
                    addBehaviour(new AttemptExchange(currentExchangeOffer));
                } else {
                    System.out.println("Invalid message format: " + inform.getContent());
                }
            }
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println(myAgent.getLocalName() + " : Household refused exchange slot request: " + refuse.getContent());
            selectedHouseholds.add(refuse.getSender());

            if(selectedHouseholds.size() == totalHouseholds){
                System.out.println("All households have completed their exchange turn.");
                resume();
            }
        }

        protected void handleFailure(ACLMessage failure) {
            System.out.println("Household failed to provide exchange slot: " + failure.getContent());
        }

        protected void handleAllResponses(Vector responses) {
            System.out.println("Received " + responses.size() + " responses for exchange slot request.");
        }

        protected void handleAgree(ACLMessage agree) {
            System.out.println("Received AGREE from household for exchange slot request: " + agree.getConversationId());
        }
    }

    private class ExchangeTicker extends TickerBehaviour {
        public ExchangeTicker(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (getHouseholdCount() == totalHouseholds && !pause) {
                addBehaviour(new InitiateExchange());
            }
        }

        @Override
        public void stop() {
            System.out.println("ExchangeTicker stopped.");
        }
    }

    private class AttemptExchange extends OneShotBehaviour{

        private ExchangeOffer exchangeOffer;

        public AttemptExchange(ExchangeOffer exchangeOffer){
            this.exchangeOffer = exchangeOffer;
        }

        @Override
        public void action() {
            if (findHouseholdForExchange(exchangeOffer) != null){
                System.out.println("Exchange possible, attempting exchange");
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(findHouseholdForExchange(exchangeOffer));
                request.setProtocol("fipa-request");
                request.setContent("exchange:" + exchangeOffer.getExchangeSlot() + ",offer-slots:" + exchangeOffer.getOfferSlots());
                request.setConversationId("exchange-" + System.currentTimeMillis() + "_" + findHouseholdForExchange(exchangeOffer).getLocalName());
                request.setReplyWith("R" + System.currentTimeMillis() + "_0");
                addBehaviour(new ExchangeInitiator(myAgent, request));
            }else{
                System.out.println("No suitable household found for exchange");
                resume();
            }
        }
    }

    private class ExchangeInitiator extends AchieveREInitiator {
        public ExchangeInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
            System.out.println("ExchangeInitiator initialized for conversation ID: " + msg.getConversationId());
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("Received INFORM from household: " + inform.getContent());
            if (inform.getContent() != null && inform.getContent().startsWith("exchange-success:")) {
                String content = inform.getContent().substring("exchange-success:".length());
                System.out.println("Exchange successful: " + content);
                resume();
            }
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Household refused exchange request: " + refuse.getContent());
            resume();
        }

        protected void handleFailure(ACLMessage failure) {
            System.out.println("Household failed to exchange: " + failure.getContent());
            resume();
        }

        protected void handleAllResponses(Vector responses) {
            System.out.println("Received " + responses.size() + " responses for exchange request.");
        }

        protected void handleAgree(ACLMessage agree) {
            System.out.println("Received AGREE from household for exchange request: " + agree.getConversationId());
        }
    }
    // DF registration
    private void registerWithDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("broker-service");
            sd.setName("broker");
            dfd.addServices(sd);
            DFService.register(this, dfd);
            System.out.println("Broker registered with DF.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause(){
        pause = true;
    }

    private void resume(){
        pause = false;
    }

    // Getter for testing
    public Map<String, AID> getHouseholdRegistry() {
        return householdRegistry;
    }


    private AID findHouseholdForExchange(ExchangeOffer exchangeOffer){
        for (Map.Entry<AID, List<Integer>> entry : initialAllocationsMap.entrySet()) {
            if (!entry.getValue().equals(exchangeOffer.getRequestAgent()) && !entry.getKey().equals(exchangeOffer.getRequestAgent())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private class ExchangeOffer{
        private AID requestAgent;
        private int exchangeSlot;
        private List<Integer> offerSlots;

        public ExchangeOffer(AID requestAgent, int exchangeSlot, List<Integer> offerSlots){
            this.requestAgent = requestAgent;
            this.exchangeSlot = exchangeSlot;
            this.offerSlots = offerSlots;
        }

        public AID getRequestAgent(){
            return requestAgent;
        }

        public int getExchangeSlot(){
            return exchangeSlot;
        }

        public List<Integer> getOfferSlots(){
            return offerSlots;
        }

        public String toString(){
            return "ExchangeOffer: requestAgent=" + requestAgent.getName() + ", exchangeSlot=" + exchangeSlot + ", offerSlots=" + offerSlots;
        }
    }
}