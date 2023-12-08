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
    //DB connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mcrs-db";
    private static final String USER = "root";
    private static final String PASS = "kk";

    protected void setup() {
        System.out.println("RecommenderAgent setup started.");
        loadConcertsFromDB();
        addBehaviour(new ConcertRequestServer());
    }

    private void loadConcertsFromDB() {
      
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM concerts")) {

            while (rs.next()) {
                int concertID = rs.getInt("concertID");
                String location = rs.getString("location");
                int ticket = rs.getInt("Ticket");
                String genre = rs.getString("genre");
                concertCatalog.add(new Concert(concertID, location, ticket, genre));
            }
            System.out.println("Concerts loaded successfully.");
        } catch (Exception e) {
            System.out.println("Error loading concerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class ConcertRequestServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                String[] details = content.split("#");
                String seekerEmail = details[0];
                String seekerLocation = details[1];
                int seekerTicket = Integer.parseInt(details[2]);
                String seekerGenre = details[3];

                // set the conversation ID for the request to distinguish between them
                String conversationId = "check-user-" + System.currentTimeMillis();

                ACLMessage userCheckRequest = new ACLMessage(ACLMessage.REQUEST);
                userCheckRequest.addReceiver(new AID("Admin", AID.ISLOCALNAME));
                userCheckRequest.setContent("CHECK_USER#" + seekerEmail);
                userCheckRequest.setConversationId(conversationId);
                myAgent.send(userCheckRequest);

                System.out.println("Sent user check request to AdminAgent for email: " + seekerEmail);

                // Wait for a response from the AdminAgent
                ACLMessage userCheckResponse = myAgent.blockingReceive(MessageTemplate.MatchConversationId(conversationId));
                if (userCheckResponse != null && userCheckResponse.getPerformative() == ACLMessage.INFORM) {
                    String[] responseParts = userCheckResponse.getContent().split("#");
                    if (responseParts.length == 2 && "USER_EXISTS".equals(responseParts[0])) {
                        int userID = Integer.parseInt(responseParts[1]);
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
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Failed to check user existence.");
                    myAgent.send(reply);
                }
            } else {
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

//The Concert inner class is found to have a concert object that holds the concert attributes (ID, location, ticketProice, and genre
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
