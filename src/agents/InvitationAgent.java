/*
 The InvitationAgent agent will be working if and only if the concert seeker received a recommendation for an upcoming concert.
 This agent is the one that doing the "find friends" task.
 After receiving the seekers preferences, this agent will search the preferences table to find all the users' IDs
 who have same preferences (potential friends).
 Then this agent will retrieve the emails for those users, and inform the concert seeker to display them in its GUI.
 */

package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.sql.SQLIntegrityConstraintViolationException;

public class InvitationAgent extends Agent {
    private Connection connection;

    protected void setup() {
        // Agent initialization behaviour
        // Create DB connection
        try {
            // JDBC driver loading
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Set up DB connection
            // Note that the username and password must match the DB server configuration. Default is user = root, pass = kk
            connection = DriverManager.getConnection("jdbc:mysql://localhost/mcrs-db", "root", "kk");
            System.out.println(getLocalName() + ": Connected to DB successfully.");
        } catch (Exception e) {
            System.err.println(getLocalName() + ": Failed to connect to DB " + e.getMessage());
            e.printStackTrace();
            doDelete();
        }
        // When the connection is ready, add the agent behaviour
        addBehaviour(new InvitationBehaviour());
    }

    private class InvitationBehaviour extends CyclicBehaviour {
        // Receive and process inbound messages
        public void action() {
            // Create the expected message template
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            // Try to receive the message
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // If the message has been received, proceed to process it
                String content = msg.getContent();
                // We pass parameters between agents using #-separated strings, so split
                String[] preferences = content.split("#");
                if (preferences.length == 4) {
                    // If the number of parameters is correct, proceed
                    String genre = preferences[0];
                    String location = preferences[1];
                    try {
                        // Cast the price
                        int ticketPrice = Integer.parseInt(preferences[2]);
                        String seekerEmail = preferences[3];
                        // Call searchForFriends to create and execute the database query
                        Set<String[]> friends = searchForFriends(seekerEmail, genre, ticketPrice, location);
                        for (String[] friend : friends) {
                            updateFriendsTable(friend[0], friend[1]); // where friend[0] is name, friend[1] is the email
                        }
                        // Prepare the reply to the original message (from the friend seeker agent)
                        ACLMessage reply = msg.createReply();
                        StringBuilder sb = new StringBuilder();
                        for (String[] friend : friends) {
                            // Add each friend to the String Builder so we can have as list of all potential friends
                            sb.append("Name: ").append(friend[0]).append(", Email: ").append(friend[1]).append("\n");
                        }
                        if (sb.length() > 0) {
                            // At least one friend found, return the results and INFORM
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(sb.toString());
                        } else {
                            // No friend found, report the FAILURE
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("No friends found with similar preferences.");
                        }
                        // Send the reply message
                        send(reply);
                    } catch (NumberFormatException e) {
                        // Generate an error message if the price is not a number
                        System.err.println(getLocalName() + ": Incorrect parsing ticket price: " + e.getMessage());
                    }
                } else {
                    // The number of parameters is not as expected (4)
                    System.err.println(getLocalName() + ": number of preferences received is in corrected. supposed to have 4, got " + preferences.length);
                }
            } else {
                // Wait for the next event to happen before proceeding with the loop
                block();
            }
        }
    }

    private Set<String[]> searchForFriends(String seekerEmail, String genre, int ticketPrice, String location) {
        // This function does the heavy lifting part of the search process, by generating a SQL query, executing it and returning the results
        // The return value is a hash set consisting of [name, email] records
        Set<String[]> friends = new HashSet<>();
        try {
            // Create the base query
            String sql = "SELECT DISTINCT u.name, u.email FROM preferences p JOIN users u ON p.userID = u.ID WHERE p.genre = ? AND p.location = ? AND p.Ticket <= ? AND u.email <> ?";
            // Prepare the base query and the connection
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                // Insert the respective fields in the query (genre, location, ticket price, email)
                pstmt.setString(1, genre);
                pstmt.setString(2, location);
                pstmt.setInt(3, ticketPrice);
                pstmt.setString(4, seekerEmail); // concert seeker email is excluded

                ResultSet rs = pstmt.executeQuery();
                // For each record corresponding to a friend: 
                while (rs.next()) {
                    // Extract the name and the email address
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    friends.add(new String[]{name, email}); // Add to set to ensure that the records are unique
                }
            }
        } catch (Exception e) {
            // Error reporting
            System.err.println(getLocalName() + ": something wrong while searching for friends. " + e.getMessage());
            e.printStackTrace();
        }
        return friends;
    }
    private void updateFriendsTable(String friendName, String friendEmail) {
        // Add user info to the friends table, no return value
        try {
            // Transaction begins here
            connection.setAutoCommit(false);
            
            // Here we are checking if this friend is already recorded / inserted
            String checkSql = "SELECT COUNT(*) FROM friends WHERE friendName = ? AND friendEmail = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                // Insert user info into the SQL query
                checkStmt.setString(1, friendName);
                checkStmt.setString(2, friendEmail);
                ResultSet checkRs = checkStmt.executeQuery();
                // Process the response to the query
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // Not inserted, so insert
                    // Create another query for inserting the new record
                    String insertSql = "INSERT INTO friends (friendName, friendEmail) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        // Insert the name and the email into the new query
                        insertStmt.setString(1, friendName);
                        insertStmt.setString(2, friendEmail);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            // Commit the transaction
            connection.commit();
        } catch (Exception e) {
            // Handler for all errors
            System.err.println(getLocalName() + ": Could not update friends table. " + e.getMessage());
            try {
                // Attempt to rollback the transaction if errors are there
                connection.rollback();
            } catch (Exception rollbackEx) {
                // This should not normally happen.
                System.err.println(getLocalName() + ": rolling back the transaction failed. " + rollbackEx.getMessage());
            }
        } finally {
            try {
                // Restore the auto-commit .. this is because I was having errors here
                connection.setAutoCommit(true);
            } catch (Exception autoCommitEx) {
                // Again, this should not normally happen
                System.err.println(getLocalName() + ": Error restoring auto-commit. " + autoCommitEx.getMessage());
            }
        }
    }



    protected void takeDown() {
        // Gracefully stop the agent by closing the connection
        try {
            // Only try to close the connection if it has not been already closed
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println(getLocalName() + ": DB connection closed.");
            }
        } catch (Exception e) {
            // The connection was open and we failed to close it
            System.err.println(getLocalName() + ": Something went wrong when closing the DB connection. " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(getLocalName() + " I am terminating.");
    }
}
