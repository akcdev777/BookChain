package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args) {
        // Get a JADE runtime instance
        Runtime rt = Runtime.instance();

        // Create a profile with GUI enabled
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable JADE GUI
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");

        int population = 8;

        // Create the main container
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Start the Introspector agent to inspect behaviors (optional)
            AgentController introspector = mainContainer.createNewAgent(
                    "introspector",
                    "jade.tools.introspector.Introspector",
                    null
            );
            introspector.start();

            // Start the Sniffer agent to monitor messages
            AgentController sniffer = mainContainer.createNewAgent(
                    "sniffer",
                    "jade.tools.sniffer.Sniffer",
                    new Object[]{} // Agents to sniff
            );
            sniffer.start();

            Thread.sleep(2000); // Wait for the sniffer to start

            // Start the BrokerAgent
            AgentController broker = mainContainer.createNewAgent(
                    "broker",
                    "test.BrokerAgent", // Fully qualified class name if in a package
                    null
            );
            broker.start();

            Thread.sleep(5000); // Wait for the sniffer to start

            for (int i = 0; i < population; i++) {
                // Start the HouseholdAgent
                AgentController household = mainContainer.createNewAgent(
                        "household" + i,
                        "test.HouseholdAgent", // Fully qualified class name if in a package
                        new Object[]{"address" + i}
                );
                household.start();
            }

        } catch (StaleProxyException e) {
            e.printStackTrace();
            System.err.println("Error launching agents: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}