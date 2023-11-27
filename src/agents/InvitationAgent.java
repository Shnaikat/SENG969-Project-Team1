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
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost/mcrs-db", "root", "kk");
            System.out.println(getLocalName() + ": Connected to the database successfully.");
        } catch (Exception e) {
            System.err.println(getLocalName() + ": Failed to connect to the database. " + e.getMessage());
            e.printStackTrace();
            doDelete();
        }

        addBehaviour(new InvitationBehaviour());
    }

    private class InvitationBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                String[] preferences = content.split("#");
                if (preferences.length == 4) {
                    String genre = preferences[0];
                    String location = preferences[1];
                    try {
                        int ticketPrice = Integer.parseInt(preferences[2]);
                        String seekerEmail = preferences[3];
                        Set<String[]> friends = searchForFriends(seekerEmail, genre, ticketPrice, location);
                        for (String[] friend : friends) {
                            updateFriendsTable(friend[0], friend[1]); // friend[0] is the name, friend[1] is the email
                        }
                        ACLMessage reply = msg.createReply();
                        StringBuilder sb = new StringBuilder();
                        for (String[] friend : friends) {
                            // Add each friend to the StringBuilder
                            sb.append("Name: ").append(friend[0]).append(", Email: ").append(friend[1]).append("\n");
                        }
                        if (sb.length() > 0) {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(sb.toString());
                        } else {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("No friends found with similar preferences.");
                        }
                        send(reply);
                    } catch (NumberFormatException e) {
                        System.err.println(getLocalName() + ": Error parsing ticket price: " + e.getMessage());
                    }
                } else {
                    System.err.println(getLocalName() + ": Incorrect number of preferences received. Expected 4, got " + preferences.length);
                }
            } else {
                block();
            }
        }
    }

    private Set<String[]> searchForFriends(String seekerEmail, String genre, int ticketPrice, String location) {
        Set<String[]> friends = new HashSet<>();
        try {
            String sql = "SELECT DISTINCT u.name, u.email FROM preferences p JOIN users u ON p.userID = u.ID WHERE p.genre = ? AND p.location = ? AND p.Ticket <= ? AND u.email <> ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, genre);
                pstmt.setString(2, location);
                pstmt.setInt(3, ticketPrice);
                pstmt.setString(4, seekerEmail); // Exclude concert seeker email

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    friends.add(new String[]{name, email}); // Add to set for uniqueness
                }
            }
        } catch (Exception e) {
            System.err.println(getLocalName() + ": Error searching for friends. " + e.getMessage());
            e.printStackTrace();
        }
        return friends;
    }
    private void updateFriendsTable(String friendName, String friendEmail) {
        try {
            // Start a transaction
            connection.setAutoCommit(false);
            
            // Check if this friend is already recorded
            String checkSql = "SELECT COUNT(*) FROM friends WHERE friendName = ? AND friendEmail = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, friendName);
                checkStmt.setString(2, friendEmail);
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // Not recorded, so insert
                    String insertSql = "INSERT INTO friends (friendName, friendEmail) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, friendName);
                        insertStmt.setString(2, friendEmail);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            // Commit the transaction
            connection.commit();
        } catch (Exception e) {
            System.err.println(getLocalName() + ": Error updating the friends table. " + e.getMessage());
            try {
                // Attempt to rollback the transaction in case of errors
                connection.rollback();
            } catch (Exception rollbackEx) {
                System.err.println(getLocalName() + ": Error rolling back the transaction. " + rollbackEx.getMessage());
            }
        } finally {
            try {
                // Restore auto-commit mode
                connection.setAutoCommit(true);
            } catch (Exception autoCommitEx) {
                System.err.println(getLocalName() + ": Error restoring auto-commit. " + autoCommitEx.getMessage());
            }
        }
    }



    protected void takeDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println(getLocalName() + ": Database connection closed.");
            }
        } catch (Exception e) {
            System.err.println(getLocalName() + ": Error while closing the database connection. " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(getLocalName() + " terminating.");
    }
}
