/*
 * This agent is acting like the service provider. 
 * If there is an upcoming concert, the Venue Agent will notify/send the information about this concert to the data manager agent. 
 * This information includes Concert Location, Ticket Price, and Genre.
 * Therefore, we can have this concert information.
 * We provide the concert information through arguments when starting this agent, so we are having different ways (variations) in our implementation.
 * For providing the arguments, we need to consider the order of the data and the number of the provided arguments. 
 */
package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class VenueAgent extends Agent {
    private String concertLocation;
    private int ticketPrice;
    private String genre;

    protected void setup() {
        System.out.println(getLocalName() + ": ready .. I am an instance of the Venue agent");

       /* Object[] args = getArguments();
        if (args != null && args.length == 3) {
            concertLocation = args[0];
            ticketPrice = Integer.parseInt((String) args[1]);
            genre =args[2];*/

        
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            concertLocation = (String) args[0];
            ticketPrice = Integer.parseInt((String) args[1]);
            genre = (String) args[2];

            // For the sake of debugging and knowing what's going on we are printing out the received arguments
            System.out.println(getLocalName() + ": received the arguments - Location: " + concertLocation + ", Price: " + ticketPrice + ", Genre: " + genre);

            
            /*ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("DataManager", AID.ISLOCALNAME));//the data manager will be the receiver 
            msg.setContent(concertLocation + "#" + ticketPrice + "#" + genre);//splitting the data using #
            send(msg);*/
            
            // Sending these details to  dataManager Agent -> should be dataManager
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("dataManager", AID.ISLOCALNAME));//the data manager will be the receiver 
            msg.setContent(concertLocation + "#" + ticketPrice + "#" + genre);//splitting the data using #
            send(msg);
        } else {
            System.out.println(getLocalName() + ": The provided No. of arguments is incorrect or Wrong separator used (Please use comma (',') as the separator)");
            doDelete();
        }
    }
}
