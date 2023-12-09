/*
 * The Recommender agent is responsible for: 
 Getting the up to date concert information. 
 Sending a user verification request to the Admin agent. According to the Admin response, the recommender will provide the seeker with a concert recommendation
 If there is any, or tell the seeker that no concerts found. 
 If the seeker is not registered the Recommender will direct him to create profile before trying to use the seek concert service.
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RecommenderAgent extends Agent {
    private List<Concert> concertCatalog = new ArrayList<>();
    // DB connection parameters
    // Note that these parameters must match the DB server configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mcrs-db";
    private static final String USER = "root";
    private static final String PASS = "kk";

    protected void setup() {
        System.out.println("RecommenderAgent setup started.");  // Debug aid
        loadConcertsFromDB();  // Load all the concerts at once and cache them
        addBehaviour(new ConcertRequestServer());  // Prepare the behaviour for responding to concert search requests
    }

    private void loadConcertsFromDB() {
        // Load all the concert data from the DB at once and cache them
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = connection.createStatement();
             // Create the SQL query
             ResultSet rs = stmt.executeQuery("SELECT * FROM concerts")) {

             // For each concert, fetch the data and add them to the concert catalog
             while (rs.next()) {
                int concertID = rs.getInt("concertID");
                String location = rs.getString("location");
                int ticket = rs.getInt("Ticket");
                String genre = rs.getString("genre");
                // Add the concert data to the catalog
                concertCatalog.add(new Concert(concertID, location, ticket, genre));
            }
            System.out.println("Concerts loaded successfully.");  // Debug
        } catch (Exception e) {
            System.out.println("Error loading concerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ConcertRequestServer extends CyclicBehaviour {
        // Wait for requests sent as messages to process them and generate replies
        public void action() {
            // Try to receive a message, if any
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // If there's a message, parse it
                String content = msg.getContent();
                // #-separated data fields; format: Email#Location#TicketPrice#Genre
                String[] details = content.split("#");
                String seekerEmail = details[0];
                String seekerLocation = details[1];
                int seekerTicket = Integer.parseInt(details[2]);
                String seekerGenre = details[3];

                // set the conversation ID for the request to distinguish between them
                String conversationId = "check-user-" + System.currentTimeMillis();

                // Authenticate the user via the admin agent
                ACLMessage userCheckRequest = new ACLMessage(ACLMessage.REQUEST);
                // Find the admin agent by name
                userCheckRequest.addReceiver(new AID("Admin", AID.ISLOCALNAME));
                userCheckRequest.setContent("CHECK_USER#" + seekerEmail);  // Used to determine that we want to authenticate the user, not create it
                userCheckRequest.setConversationId(conversationId);
                myAgent.send(userCheckRequest);

                System.out.println("Sent user check request to AdminAgent for email: " + seekerEmail);  // Debug aid

                // Wait for a response from the AdminAgent
                // We use a blocking receive to ensure we will wait for the response
                // The conversation ID is used to track the reply and make sure it matches to the correct request
                ACLMessage userCheckResponse = myAgent.blockingReceive(MessageTemplate.MatchConversationId(conversationId));
                if (userCheckResponse != null && userCheckResponse.getPerformative() == ACLMessage.INFORM) {
                    // A non-null response shows the operation has been successful. 
                    // The #-separated reply obeys the format "USER_EXISTS"#userid, assuming the user does exist
                    String[] responseParts = userCheckResponse.getContent().split("#");
                    if (responseParts.length == 2 && "USER_EXISTS".equals(responseParts[0])) {
                        // We need the user ID to correctly update user records
                        int userID = Integer.parseInt(responseParts[1]);
                        // Submit the user preferences to the preferences table, which will be later used for matching potential friends
                        updateUserPreferences(seekerLocation, seekerTicket, seekerGenre, userID);
                        // Call proposeConcert here
                        recommendConcert(seekerLocation, seekerTicket, seekerGenre, msg);
                    } else {
                        // User not registered -> send a refusal message
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("User not found. Please register.");
                        myAgent.send(reply);
                    }
                } else {
                    // This section would be triggered in case the admin agent does not reply properly
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Failed to check user existence.");
                    myAgent.send(reply);
                }
            } else {
                // If there's no message to process, wait for the next event. 
                block();
            }
        }
    }

    private void updateUserPreferences(String location, int ticketPrice, String genre, int userID) {
        // insert the user's preferences to DB
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO preferences (location, Ticket, genre, userID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, location);
                stmt.setInt(2, ticketPrice);
                stmt.setString(3, genre);
                stmt.setInt(4, userID);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Preferences updated .. the affected rows: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("could not update user preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recommendConcert(String location, int ticketPrice, String genre, ACLMessage seekerMsg) {
        ACLMessage proposal = seekerMsg.createReply();
        boolean concertFound = false;
        for (Concert concert : concertCatalog) {
            if (concert.location.equalsIgnoreCase(location) && 
                concert.ticketPrice <= ticketPrice && 
                concert.genre.equalsIgnoreCase(genre)) {
                proposal.setContent(" matches your preferences exactly, It has the ID" + concert.concertID + 
                        ", Location: " + concert.location +
                        ", Ticket Price: " + concert.ticketPrice + 
                        ", and it is of " + concert.genre + " genre: ");
                proposal.setPerformative(ACLMessage.PROPOSE);
                concertFound = true;
                break;
            }
        }
        if (!concertFound) {
            proposal.setContent("No matching concerts found.");
            proposal.setPerformative(ACLMessage.REFUSE);
        }
        send(proposal);
    }

// The Concert inner class is found to have a concert object that holds the concert attributes (ID, location, ticketProice, and genre
    public static class Concert {
        int concertID;
        String location;
        int ticketPrice;
        String genre;

        Concert(int concertID, String location, int ticketPrice, String genre) {
            this.concertID = concertID;
            this.location = location;
            this.ticketPrice = ticketPrice;
            this.genre = genre;
        }

        @Override
        public String toString() {
            return "ConcertID: " + concertID + ", Location: " + location +
                    ", Ticket Price: " + ticketPrice + ", Genre: " + genre;
        }
    }
}
