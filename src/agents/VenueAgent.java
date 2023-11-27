package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class VenueAgent extends Agent {
    private String concertLocation;
    private int ticketPrice;
    private String genre;

    protected void setup() {
        System.out.println(getLocalName() + ": VenueAgent is ready.");

        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            concertLocation = (String) args[0];
            ticketPrice = Integer.parseInt((String) args[1]);
            genre = (String) args[2];

            // Log the received arguments
            System.out.println(getLocalName() + ": Received the arguments - Location: " + concertLocation + ", Price: " + ticketPrice + ", Genre: " + genre);

            // Sending these details to the DataManagerAgent
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("dataManager", AID.ISLOCALNAME));
            msg.setContent(concertLocation + "#" + ticketPrice + "#" + genre);
            send(msg);
        } else {
            System.out.println(getLocalName() + ": Incorrect number of arguments provided.");
            doDelete();
        }
    }
}
