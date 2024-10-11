import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import agents.*;
public class Main {

    public static void main(String[] args) {
        // Get the JADE runtime instance
        Runtime rt = Runtime.instance();

        // Create the main JADE container
        Profile profile = new ProfileImpl("localhost", 1099, "MyPlatform");
        System.out.println("Launching the main container..." + profile);
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Deploy the BookBuyerAgent
            String bookToBuy = "Effective Java";
            Object[] buyerArgs = new Object[]{bookToBuy};
            System.out.println("Starting Book Buyer Agent...");
            AgentController buyerAgent = mainContainer.createNewAgent("buyer", "agents.BookBuyerAgent", buyerArgs);
            buyerAgent.start();

            // Deploy the BookSellerAgent
            String bookTitle = "Effective Java";
            String price = "150"; // Book price
            String quantity = "1"; // Available stock
            Object[] sellerArgs = new Object[]{bookTitle, price, quantity};
            System.out.println("Starting Book Seller Agent...");
            AgentController sellerAgent = mainContainer.createNewAgent("seller1", "agents.BookSellerAgent", sellerArgs);
            sellerAgent.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
