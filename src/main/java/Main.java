import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args) {
        // Get the JADE runtime instance
        Runtime rt = Runtime.instance();

        // Create the main JADE container
        Profile profile = new ProfileImpl("localhost", 1099, "MyPlatform");
        System.out.println("Launching the main container..." + profile);
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Array of books buyers want to buy
            String[][] buyerArgsArray = {
                {"Effective Java"},
                {"Clean Code"},
                {"Design Patterns"},
                {"Refactoring"},
                {"The Pragmatic Programmer"}
            };

            // Deploy multiple BookBuyerAgents
            for (int i = 0; i < buyerArgsArray.length; i++) {
                System.out.println("Starting Book Buyer Agent " + (i + 1) + "...");
                Object[] buyerArgs = buyerArgsArray[i];
                AgentController buyerAgent = mainContainer.createNewAgent("buyer" + (i + 1), "agents.BookBuyerAgent", buyerArgs);
                buyerAgent.start();
            }

            // Array of sellers with book titles, prices, and quantities
            String[][] sellerArgsArray = {
                {"Effective Java", "150", "10"},
                {"Clean Code", "120", "8"},
                {"Design Patterns", "200", "5"},
                {"Refactoring", "180", "7"},
                {"The Pragmatic Programmer", "130", "6"},
                {"Head First Java", "100", "12"},
                {"Java Concurrency in Practice", "160", "4"},
                {"Spring in Action", "140", "10"}
            };

            // Deploy multiple BookSellerAgents
            for (int i = 0; i < sellerArgsArray.length; i++) {
                System.out.println("Starting Book Seller Agent " + (i + 1) + "...");
                Object[] sellerArgs = sellerArgsArray[i];
                AgentController sellerAgent = mainContainer.createNewAgent("seller" + (i + 1), "agents.BookSellerAgent", sellerArgs);
                sellerAgent.start();
            }

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
