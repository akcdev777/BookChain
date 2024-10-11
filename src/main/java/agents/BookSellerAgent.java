package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.HashMap;
import java.util.Map;

public class BookSellerAgent extends Agent{

    private static class  BookInfo {
        int price;
        int quantity;

        BookInfo(int price, int quantity){
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        public String toString(){
            return "Price: " + price + ", Quantity: " + quantity;
        }
    }//end

    private Map<String, BookInfo> catalog;

    protected void setup(){
        System.out.println("[ " + getAID().getName() + "] agent is ready");

        catalog = new HashMap<>();

        Object[] args = getArguments();

        if (args != null && args.length > 0){
            if (args.length % 3 != 0){
                System.out.println("Invalid number of arguments. Each book should have a title price and quantity");
                doDelete();
                return;
            }
            
            for (int i = 0; i < args.length; i += 3){
                String bookTitle = (String) args[i];
                int bookPrice;
                int quantity;

                try{
                    bookPrice = Integer.parseInt((String) args[i + 1]);
                    quantity = Integer.parseInt((String) args[i + 2]);

                }catch(NumberFormatException e){
                    System.out.println("Invalid price or quantity for book: " + bookTitle);
                    doDelete();
                    return;
                }

                catalog.put(bookTitle, new BookInfo(bookPrice, quantity));
                System.out.println("Added to catalog : " + bookTitle + " | Price " + bookPrice + " | Quantity " + quantity);
            }
        }
        else{
            System.out.println("No books provided. Terminating agent");
            doDelete();
            return;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-seller");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[" + getAID().getName() + "] registered successfully with DF");
        } catch (FIPAException e) {
            System.out.println("[" + getAID().getName() + "] failed to register with DF");
            e.printStackTrace();
        }


    }

    protected void takeDown() {
        // Deregister the seller agent from the DF
        try {
            DFService.deregister(this);
            System.out.println("[" + getAID().getName() + "] successfully deregistered from the DF.");
        } catch (FIPAException e) {
            System.out.println("[" + getAID().getName() + "] failed to deregister from the DF: " + e.getMessage());
        }
        System.out.println("[" + getAID().getName() + "] agent terminated.");
    }



    
}
