//DataManager Agent Code.

/*DataManager Agent can listen to the Venue Agent. Venue Agent 
provides the new concert information, including location, ticket Price, and genre. After receiving this information from the Venue (provider), 
the DataManager Agent inserts them in the DataBase (Concert tables).
Two messages might be shown in this process:
1) Concert details were inserted into the database successfully.
2) Error inserting concert details into the database.*/
    
/*
 * The data manger will be responsible for acquiring the upcoming concerts information. 
 * This agent acts as out gateway agent, as he will receive these information from the Venue agent, 
 * then update the concerts table, so when the recommender agent search for matching concert, 
 * he will find the up-to-date concerts information in the concert table 
 */
package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DataManagerAgent extends Agent {
    private Connection conn;

    protected void setup() {
        System.out.println(getLocalName() + ": DataManagerAgent is ready.");

        // Establish a connection to the DB
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mcrs-db", "root", "kk");
            System.out.println("Database connected successfully."); // check if Db connectected successfully
            conn.setAutoCommit(true); // enable auto-commit to be sure that the transaction reflected on the DB
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("I can't connect to DB " + e.getMessage()); //Gives this error message if connection to the DataBase fails
            doDelete();
        }

        addBehaviour(new InsertConcertDetailsBehaviour());
    }

    private class InsertConcertDetailsBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println(getLocalName() + ": Received a message.");
                String content = msg.getContent();
                String[] details = content.split("#");
                if(details.length == 3) {
                    String location = details[0];
                    int price = Integer.parseInt(details[1]);
                    String genre = details[2];
                 // Print incoming arguments (concert details)
                    System.out.println("I am the data manager and I received the following concert details: Location -> " + location + ", Price -> " + price + ", Genre -> " + genre); 
                    updateConcertDetails(location, price, genre);//Sending those info to be inserted in concerts table
                }
            } else {
                block();
            }
        }

        private void updateConcertDetails(String location, int price, String genre) {
            String query = "INSERT INTO concerts(location, Ticket, genre) VALUES(?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, location);
                pstmt.setInt(2, price);
                pstmt.setString(3, genre);
                
                int rows = pstmt.executeUpdate();
                System.out.println(rows + " row(s) inserted."); // Be sure the entered args were inserted correctly to concerts table
                
                if (conn.getWarnings() != null) { // check for unexpected errors from DB
                    System.out.println("SQL Warning: " + conn.getWarnings());
                }
                
                System.out.println(getLocalName() + ": Concert details inserted into the concerts table successfully by the data manager agent.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Data manager could not insert the concert details into the DB " + e.getMessage());
            }
        }
    }
}
