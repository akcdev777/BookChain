import agents.*;
import jade.wrapper.StaleProxyException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class Main {
    public static void main(String[] args) throws StaleProxyException{
        Runtime rt = Runtime.instance();

        Profile pMain = new ProfileImpl("localhost", 1099, "MyPlatform");
        System.out.println("Launching the main container..." + pMain);

        //Create the main container
        ContainerController mainContainer = rt.createMainContainer(pMain);

        //Deploy BookBuyerAgent
        String bookToBuy = "Effective Java";
        Object[] agentsArgs = new Object[]{bookToBuy};

        System.out.println("Starting Book Buyer Agent ... ");
        AgentController agentController = mainContainer.createNewAgent("buyer", BookBuyerAgent.class.getName(), agentsArgs);

        agentController.start();

        System.out.println("Initializing Seller Agent ...");

        Object[] sellerArgs1 = new Object[]{"Effective Java", "150", "10"};

        AgentController sellerAgent1 = mainContainer.createNewAgent("seller1", BookSellerAgent.class.getName(), sellerArgs1);

        sellerAgent1.start();
    }
    
}
